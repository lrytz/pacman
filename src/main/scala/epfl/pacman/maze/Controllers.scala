package epfl.pacman
package maze

import actors.Actor
import java.awt.Rectangle

trait Controllers { this: MVC =>
  
  val controller: Controller

  class Controller extends Actor {
    var pacmanBehavior = new Behavior()
    val monsterBehavior = new Behavior()

    // @TODO: maybe put these into the model?
    private var tickCounter = 0
    private var dieCounter = 0

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

              if (tickCounter == 0) {
                // compute next block position if all the small steps have been painted
                tickCounter = Settings.blockSize
                val newPacman = {
                  val (pos, dir) = pacmanBehavior.next(model, model.pacman)
                  PacMan(makeOffsetPosition(pos, dir), dir, model.pacman.angle)
                }

                val newMonsters = model.monsters.map(monster => {
                  val (pos, dir) = monsterBehavior.next(model, monster)
                  Monster(makeOffsetPosition(pos, dir), dir, monster.laser.copy(status = model.clearPathBetween(monster, model.pacman)))
                })

                model = model.copy(newPacman, newMonsters)

              }

              // update the figure's offsets
              tickCounter -= 1
              model.pacman.incrOffset
              model.pacman.incrAngle

              // @TODO: repaint old location when figure moves out of the grid (donut)

              view.repaint(figureRect(model.pacman))
              for (monster <- model.monsters) {
                monster.incrOffset
                monster.incrAnimOffset
                view.repaint(figureRect(monster))
              }

              if (model.monsters.exists(m => {
                m.pos.overlaps(model.pacman.pos)
              })) {
                pause()
                dieCounter = Settings.ticksToDie
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
