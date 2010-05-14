package epfl.pacman
package maze

trait Thingies { this: MVC =>

  var id = 0
  def freshId = { id += 1; id }
  
  abstract class Thingy(val pos: Position, id: Int = freshId)

  abstract class Figure(override val pos: OffsetPosition, val dir: Direction) extends Thingy(pos) {
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

  case class PacMan(override val pos: OffsetPosition, override val dir: Direction, val angle: Angle) extends Figure(pos, dir) {
    def incrAngle {
      angle.counter = (angle.counter + 1) % 60
    }
  }

  case class Monster(override val pos: OffsetPosition, override val dir: Direction, val laser: LaserSettings) extends Figure(pos, dir) {
    def incrAnimOffset {
      laser.animOffset = (laser.animOffset + 1) % 3
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
