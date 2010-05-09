package epfl.pacman
package maze

trait Positions { this: MVC =>

  sealed abstract class Position {
    val x: Int
    val y: Int

    def onTop    = new BlockPosition(x, y-1)
    def onLeft   = new BlockPosition(x-1, y)
    def onBottom = new BlockPosition(x, y+1)
    def onRight  = new BlockPosition(x+1, y)

    def nextIn(dir: Direction) = dir match {
      case Up    => onTop
      case Left  => onLeft
      case Down  => onBottom
      case Right => onRight
    }

    override def hashCode = x + y

    override def equals(other: Any) = other match {
      case pos: Position => x == pos.x && y == pos.y
      case _ => false
    }
  }

  case class BlockPosition(val x: Int, val y: Int) extends Position

  case class OffsetPosition(val x: Int, val y: Int, var xo: Int = 0, var yo: Int = 0) extends Position {
    def overlaps(other: OffsetPosition) = {
      val selfX = x * Settings.blockSize + xo
      val selfY = y * Settings.blockSize + yo

      val otherX = other.x * Settings.blockSize + other.xo
      val otherY = other.y * Settings.blockSize + other.yo

      (math.abs(selfX - otherX) < Settings.overlapThreshold) &&
      (math.abs(selfY - otherY) < Settings.overlapThreshold)
    }
  }

}
