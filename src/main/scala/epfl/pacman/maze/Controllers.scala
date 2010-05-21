package epfl.pacman
package maze

import actors.Actor
import java.awt.Rectangle
import scala.util.Random.nextInt
import behaviour.{DefaultMonsterBehavior, DefaultPacManBehavior, Behavior}

trait Controllers { mvc: MVC =>

  class Controller extends Actor {
    var pacmanBehavior: Behavior { val mvc: Controllers.this.type } = new DefaultPacManBehavior {
      val mvc: Controllers.this.type = Controllers.this
    }

    val monsterBehavior = new DefaultMonsterBehavior {
      val mvc: Controllers.this.type = Controllers.this
    }

    // @TODO: maybe put these into the model?
    private var tickCounter = 0
    private var dieCounter = 0
    private var hunterCounter = 0
    private var revivals: List[(Int, Monster)] = Nil

    def pause() { model = model.copy(paused = true) }
    def resume() { model = model.copy(paused = false) }

    private def makeOffsetPosition(to: Position, dir: Direction, stopped: Boolean) = {
      val s = Settings.blockSize

      val (xo, yo) = if(!stopped) {
        dir match {
          case Up    => (0, s)
          case Left  => (s, 0)
          case Down  => (0, -s)
          case Right => (-s, 0)
        }
      } else {
        (0, 0)
      }

      @inline def donut(i: Int, s: Int) = (i + s) % s
      OffsetPosition(donut(to.x, Settings.hBlocks), donut(to.y, Settings.vBlocks), xo, yo)
    }

    @inline final def figureRect(f: Figure) = {
      val pos = f.pos
      val s = Settings.blockSize
      new Rectangle(pos.x*s + pos.xo - 1, pos.y*s + pos.yo - 1, s + 2, s + 2)
    }

    def validateDir(model: Model, f: Figure, optDir: Option[Direction]): (Position, Direction, Boolean) = {
        // Make sure the direction is possible
        optDir match {
            case Some(dir) if !model.isWallAt(f.pos.nextIn(dir)) =>
                (f.pos.nextIn(dir), dir, false)
            case _ =>
                (f.pos, f.dir, true)
        }
    }

    def act() {
      loop {
        react {
          case Tick =>
            if (!model.paused) {

              if (tickCounter == 0 || (tickCounter == Settings.blockSize/2 && model.pacman.mode == Hunter)) {
                val majorTick = tickCounter == 0

                var newMonsters = if (majorTick) {
                  tickCounter = Settings.blockSize

                  // compute next block position if all the small steps have been painted
                  model.monsters.map(monster => {
                    val (pos, dir, stopped) = validateDir(model, monster, monsterBehavior.next(model, monster))
                    val laserMode  = (model.minDistBetween(monster.pos, model.pacman.pos) < 10) && model.pacman.mode == Hunted
                    Monster(makeOffsetPosition(pos, dir, stopped), dir, stopped, monster.laser.copy(status = laserMode))
                  })
                } else {
                  model.monsters
                }

                var newPacman = model.pacman

                if (hunterCounter > 0) {
                  hunterCounter -= 1
                }

                if (majorTick) {
                  if(revivals.exists(_._1 == 0)) {
                    for ((tick, monst) <- revivals if tick == 0) {
                      val pos = model.randomValidPos
                      newMonsters += monst.copy(makeOffsetPosition(pos, Right, false),  Right, false, monst.laser.copy(status = false))
                    }
                    revivals = revivals.filter(_._1 != 0)
                  }

                  // decrement counters
                  revivals = revivals.map(r => (r._1-1, r._2))

                  if (newPacman.mode == Hunter && hunterCounter == 0) {
                    newPacman = newPacman.copy(mode = Hunted)
                  }
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

                val (pos, dir, stopped) = validateDir(model, newPacman, pacmanBehavior.next(model, newPacman))
                newPacman = newPacman.copy(makeOffsetPosition(pos, dir, stopped), dir, stopped)

                model = model.copy(pacman = newPacman, monsters = newMonsters, points = newPoints)
              }

              tickCounter -= 1

              // update the figure's offsets
              if (model.pacman.mode == Hunter) {
                if (!model.pacman.stopped) {
                  model.pacman.incrOffset
                  model.pacman.incrOffset
                }
                model.pacman.incrAngle
              } else {
                if (!model.pacman.stopped) {
                  model.pacman.incrOffset
                }
                model.pacman.incrAngle
              }

              view.repaint(figureRect(model.pacman))

              // Repaint borders so that the donut doesn't leave traces
              val s = Settings.blockSize
              view.repaint(new Rectangle(0, 0, s, Settings.vBlocks*s))
              view.repaint(new Rectangle((Settings.hBlocks-1)*s, 0, Settings.hBlocks*s, Settings.vBlocks*s))

              for (monster <- model.monsters) {
                if (!monster.stopped) {
                  monster.incrOffset
                }
                monster.incrAnimOffset
                view.repaint(figureRect(monster))
              }

              val omonst = model.monsters.find(m => { m.pos.overlaps(model.pacman.pos) })
              if (!omonst.isEmpty) {
                if (model.pacman.mode == Hunter) {
                  // eat the monster
                  model = model.copy(monsters = model.monsters - omonst.get)
                  revivals = (nextInt(10)+10, omonst.get) :: revivals
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
            //assert(model.paused, "trying to load new model while game is running")
            pacmanBehavior = newPacmanBehavior
            resume()

          case Error(line) =>
            
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
  case class Load(pacmanBehavior: Behavior { val mvc: Controllers.this.type })
  case class Error(line: Int)
}
