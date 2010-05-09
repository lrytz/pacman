package epfl.pacman
package maze

import actors.Actor

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
      OffsetPosition(to.x, to.y, xo, yo)
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
                  Figure(makeOffsetPosition(pos, dir), dir)
                }

                val newMonsters = model.monsters.map(monster => {
                  val (pos, dir) = monsterBehavior.next(model, monster)
                  Figure(makeOffsetPosition(pos, dir), dir)
                })

                model = model.copy(newPacman, newMonsters)

              }

              // update the figure's offsets
              tickCounter -= 1
              model.pacman.incrOffset()
              for (monster <- model.monsters) monster.incrOffset()

              if (model.monsters.exists(m => {
                m.pos.overlaps(model.pacman.pos)
              })) {
                pause()
                dieCounter = Settings.ticksToDie
              }

              view.repaint()

            } else if (dieCounter > 0) {
              dieCounter -= 1
              if (dieCounter == 0) {
                tickCounter = 0
                model = model.randomizeFigures()
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