package epfl.pacman
package maze

trait Thingies { this: MVC =>

  var id = 0
  def freshId = { id += 1; id }

  abstract class Point(val pos: Position)
  case class NormalPoint(override val pos: Position) extends Point(pos)
  case class SuperPoint(override val pos: Position) extends Point(pos)

  abstract class Thingy(val pos: Position)

  abstract class Figure(override val pos: OffsetPosition, val stopped: Boolean, val dir: Direction) extends Thingy(pos) {
    def incrOffset {
      dir match {
        case Up    => pos.yo -= 1
        case Left  => pos.xo -= 1
        case Down  => pos.yo += 1
        case Right => pos.xo += 1
      }
    }
  }

  case class Angle(var counter: Int) {
    def value = if (counter > 30) 60-counter else counter
  }

  case class LaserSettings(var status: Boolean, var animOffset: Int = 0)

  sealed abstract class Mode
  case object Hunted extends Mode
  case object Hunter extends Mode

  case class PacMan(override val pos: OffsetPosition,
                    override val stopped: Boolean,
                    override val dir: Direction,
                    val mode: Mode,
                    val angle: Angle) extends Figure(pos, stopped, dir) {
    def incrAngle {
      angle.counter = (angle.counter + 1) % 60
    }
  }

  case class Monster(override val pos: OffsetPosition,
                     override val stopped: Boolean,
                     override val dir: Direction,
                     val laser: LaserSettings,
                     val id: Int = freshId) extends Figure(pos, stopped, dir) {
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

  case class Wall(override val pos: Position) extends Thingy(pos)

}
