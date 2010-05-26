package epfl.pacman
package maze

import actors.Actor
import java.awt.Rectangle
import scala.util.Random.nextInt
import behaviour.{DefaultMonsterBehavior, DefaultPacManBehavior, Behavior}

trait Controllers { mvc: MVC =>

  class Controller extends Actor {
    private var pacmanBehavior: Behavior { val mvc: Controllers.this.type } = new DefaultPacManBehavior {
      val mvc: Controllers.this.type = Controllers.this
    }

    private val monsterBehavior = new DefaultMonsterBehavior {
      val mvc: Controllers.this.type = Controllers.this
    }

    private def tickCounter = model.counters('tick)
    private def tickCounter_=(v: Int) { model.counters('tick) = v }
    private def dieCounter = model.counters('die)
    private def dieCounter_=(v: Int) { model.counters('die) = v }
    private def hunterCounter = model.counters('hunter)
    private def hunterCounter_=(v: Int) { model.counters('hunter) = v }
    private object revivals {
      def apply(m: Monster) = model.counters(m)
      def update(m: Monster, v: Int) = model.counters(m) = v
    }

    private def pause(msg: String) { model = model.copy(paused = true, message = msg) }
    private def resume() { model = model.copy(paused = false) }

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

    @inline private def figureRect(f: Figure) = {
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
                    val laserMode  = (model.minDistBetween(monster.pos, monster.pos, model.pacman.pos) < 10) && model.pacman.mode == Hunted
                    Monster(makeOffsetPosition(pos, dir, stopped), dir, stopped, monster.laser.copy(status = laserMode))
                  })
                } else {
                  model.monsters
                }

                var newDeadMonsters = model.deadMonsters

                var newPacman = model.pacman

                if (hunterCounter > 0) {
                  hunterCounter -= 1
                }

                if (majorTick) {
                  for (m <- model.deadMonsters) {
                    revivals(m) -= 1
                    if (revivals(m) == 0) {
                      val pos = model.randomValidPos
                      newMonsters += m.copy(makeOffsetPosition(pos, Right, false),  Right, false, m.laser.copy(status = false))
                      newDeadMonsters -= m
                    }
                  }

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

                model = model.copy(pacman = newPacman, monsters = newMonsters,
                                   points = newPoints, deadMonsters = newDeadMonsters)
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
                  model = model.copy(monsters = model.monsters - omonst.get, deadMonsters = model.deadMonsters + omonst.get)
                  revivals(omonst.get) = nextInt(10)+10
                } else {
                  val msg =
                    if (model.simpleMode) {
                      "Game over..."
                    } else {
                      model = model.copy(pacman = model.pacman.copy(lives = model.pacman.lives-1))
                      if (model.pacman.lives > 0) {
                        "Vie perdu!"
                      } else {
                        "Game over..."
                      }
                    }
                  pause(msg)
                  dieCounter = Settings.ticksToDie
                }
              }

            } else if (dieCounter > 0) {
              dieCounter -= 1
              if (dieCounter == 0) {
                if (!model.simpleMode && model.pacman.lives > 0) {
                  model = model.resetFigures()
                  view.repaint() // full repaint to get rid of old figures
                  resume()
                }
              }
            }

          case Pause(msg) =>
            pause(msg)
            view.repaint()

          case Resume =>
            resume()
            view.repaint()

          case Load(newPacmanBehavior) =>
            pacmanBehavior = newPacmanBehavior
            gui.unlock()
            gui.resume()
            resume()

          case Reset(simpleMode) =>
            model = new Model(simpleMode = simpleMode)
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
  case class Pause(msg: String = "Jeu en pause...")
  case object Resume
  case class Reset(simpleMode: Boolean)
  case class Compile(code: String)
  case class Load(pacmanBehavior: Behavior { val mvc: Controllers.this.type })
}
