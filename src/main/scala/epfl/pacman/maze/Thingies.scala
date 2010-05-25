package epfl.pacman
package maze

trait Thingies { this: Models =>

  private var id = 0
  private def freshId = { id += 1; id }

  abstract class Thingy { val pos: Position }

  abstract class Figure extends Thingy {
    override val pos: OffsetPosition
    val dir: Direction
    val stopped: Boolean

    def incrOffset {
      dir match {
        case Up    => pos.yo -= 1
        case Left  => pos.xo -= 1
        case Down  => pos.yo += 1
        case Right => pos.xo += 1
      }
    }
  }

  /**
   * PacMan
   */

  sealed abstract class Mode
  case object Hunted extends Mode
  case object Hunter extends Mode

  case class Angle(var counter: Int) {
    def value = if (counter > 30) 60-counter else counter
  }

  case class PacMan(pos: OffsetPosition,
                    dir: Direction,
                    stopped: Boolean = true,
                    mode: Mode = Hunted,
                    angle: Angle = Angle(0),
                    lives: Int = Settings.nbLives) extends Figure {
    def incrAngle {
      angle.counter = (angle.counter + 2) % 60
    }
  }


  /**
   * Monsters
   */

  case class LaserSettings(var status: Boolean, var animOffset: Int)

  case class Monster(pos: OffsetPosition,
                     dir: Direction,
                     stopped: Boolean = false,
                     laser: LaserSettings = LaserSettings(true, 0),
                     id: Int = freshId) extends Figure {
    def incrAnimOffset {
      laser.animOffset = (laser.animOffset + 1) % 6
    }

    def activateLaser {
      laser.status = true
    }

    def deactivateLaser {
      laser.status = false
    }
  }

  /**
   * Walls
   */

  case class Wall(pos: Position) extends Thingy


  /**
   * Points
   */

  case class NormalPoint(pos: Position) extends Thingy
  case class SuperPoint(pos: Position) extends Thingy
}
