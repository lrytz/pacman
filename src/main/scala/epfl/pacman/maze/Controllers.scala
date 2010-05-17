package epfl.pacman
package maze

import actors.Actor
import java.awt.Rectangle

trait Controllers { this: MVC =>
  
  val controller: Controller

  class Controller extends Actor {
    var pacmanBehavior  = new Behavior()
    val monsterBehavior = new Behavior()
    val structuredModel = new StructuredModel(new Model)

    // @TODO: maybe put these into the model?
    private var tickCounter = 0
    private var dieCounter = 0
    private var hunterCounter = 0

    def pause() { model = model.copy(paused = true) }
    def resume() { model = model.copy(paused = false) }

    private def makeOffsetPosition(to: Position, dir: Direction) = {
      val s = Settings.blockSize
      
      val (xo, yo) = dir match {
        case Up    => (0, s)
        case Left  => (s, 0)
        case Down  => (0, -s)
        case Right => (-s, 0)
      }

      @inline def donut(i: Int, s: Int) = (i + s) % s
      OffsetPosition(donut(to.x, Settings.hBlocks), donut(to.y, Settings.vBlocks), xo, yo)
    }

    @inline final def figureRect(f: Figure) = {
      val pos = f.pos
      val s = Settings.blockSize
      new Rectangle(pos.x*s + pos.xo - 1, pos.y*s + pos.yo - 1, s + 2, s + 2)
    }

    def act() {
      loop {
        react {
          case Tick =>
            if (!model.paused) {

              if (tickCounter == 0 || (tickCounter == Settings.blockSize/2 && model.pacman.mode == Hunter)) {

                val newMonsters = if (tickCounter == 0) {
                  tickCounter = Settings.blockSize

                  // compute next block position if all the small steps have been painted
                  model.monsters.map(monster => {
                    val (pos, dir) = monsterBehavior.next(model, monster)
                    val laserMode  = structuredModel.minDistBetween(monster.pos, model.pacman.pos) < 10
                    Monster(makeOffsetPosition(pos, dir), dir, monster.laser.copy(status = laserMode))
                  })
                } else {
                  model.monsters
                }

                var newPacman = {
                  val (pos, dir) = pacmanBehavior.next(model, model.pacman)
                  model.pacman.copy(pos = makeOffsetPosition(pos, dir), dir =  dir)
                }

                val p = model.points.find(p => p.pos == model.pacman.pos)

                val newPoints = if (!p.isEmpty) {
                    if (p.get.isInstanceOf[SuperPoint]) {
                        newPacman = newPacman.copy(mode = Hunter)
                        hunterCounter = Settings.ticksToHunt
                    }
                    model.points - p.get
                } else {
                    model.points
                }

                if (hunterCounter > 0) {
                  hunterCounter -= 1
                  if (hunterCounter == 0) {
                    newPacman = newPacman.copy(mode = Hunted)
                  }
                }
                model = model.copy(pacman = newPacman, monsters = newMonsters, points = newPoints)
              }

              tickCounter -= 1

              // update the figure's offsets
              if (model.pacman.mode == Hunter) {
                model.pacman.incrOffset
                model.pacman.incrOffset
                model.pacman.incrAngle
              } else {
                model.pacman.incrOffset
                model.pacman.incrAngle
              }

              // @TODO: repaint old location when figure moves out of the grid (donut)

              view.repaint(figureRect(model.pacman))
              for (monster <- model.monsters) {
                monster.incrOffset
                monster.incrAnimOffset
                view.repaint(figureRect(monster))
              }

              val omonst = model.monsters.find(m => { m.pos.overlaps(model.pacman.pos) })
              if (!omonst.isEmpty) {
                if (model.pacman.mode == Hunter) {
                  // eat the monster
                  model = model.copy(monsters = model.monsters - omonst.get)
                } else {
                  pause()
                  dieCounter = Settings.ticksToDie
                }
              }

            } else if (dieCounter > 0) {
              dieCounter -= 1
              if (dieCounter == 0) {
                tickCounter = 0
                model = model.randomizeFigures()
                view.repaint() // rull repaint to get rid of old figures
                resume()
              }
            }

          case Pause =>
            pause()

          case Resume =>
            resume()

          case Load(newPacmanBehavior) =>
            assert(model.paused, "trying to load new model while game is running")
            pacmanBehavior = newPacmanBehavior
            resume()

        }
      }
    }
  }

  object ticker extends Actor {
    def act() {
      while(true) {
        controller ! Tick
        Thread.sleep(Settings.sleepTime)
      }
    }
  }


  case object Tick
  case object Pause
  case object Resume
  case class Load(pacmanBehavior: Behavior)
}
