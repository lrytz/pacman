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

  case class Angle(var counter: Int) {
    def value = if (counter > 30) 60-counter else counter
  }

  case class PacMan(pos: OffsetPosition,
                    dir: Direction,
                    stopped: Boolean = true,
                    hunter: Boolean = false,
                    angle: Angle = Angle(0),
                    lives: Int = Settings.nbLives) extends Figure {
    def incrAngle {
      angle.counter = (angle.counter + 2) % 60
    }
  }


  /**
   * Monsters
   */

  sealed abstract class MonsterTyp
  object Cerebro extends MonsterTyp
  object Info extends MonsterTyp

  case class AnimationSettings(var status: Boolean, var animOffset: Int)

  case class Monster(pos: OffsetPosition,
                     dir: Direction,
                     typ: MonsterTyp,
                     stopped: Boolean = false,
                     anim: AnimationSettings = AnimationSettings(true, 0),
                     id: Int = freshId) extends Figure {
    def incrAnimOffset {
      anim.animOffset = (anim.animOffset + 1) % 6
    }

    def activateAnim {
      anim.status = true
    }

    def deactivateAnim {
      anim.status = false
    }
  }

  /**
   * Walls
   */

  case class Wall(pos: Position, tpe: WallType = BlueWall) extends Thingy

  sealed abstract class WallType

  case object BlueWall extends WallType
  case object RedWall  extends WallType
  case object NoWall   extends WallType


  /**
   * Points
   */

  case class NormalPoint(pos: Position) extends Thingy
  case class SuperPoint(pos: Position) extends Thingy
}
