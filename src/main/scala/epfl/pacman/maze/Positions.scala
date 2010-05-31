package epfl.pacman
package maze

trait Positions { this: Models =>

  sealed abstract class Position {
    val x: Int
    val y: Int

    def onTop    = new BlockPosition(x, y-1)
    def onLeft   = new BlockPosition((x-1+Settings.hBlocks) % Settings.hBlocks, y)
    def onBottom = new BlockPosition(x, y+1)
    def onRight  = new BlockPosition((x+1) % Settings.hBlocks, y)

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

  case class BlockPosition(x: Int, y: Int) extends Position

  case class OffsetPosition(x: Int, y: Int, var xo: Int = 0, var yo: Int = 0) extends Position {
    def overlaps(other: OffsetPosition) = {
      val s = Settings.blockSize

      val selfX = x * s + xo
      val selfY = y * s + yo

      val otherX = other.x * s + other.xo
      val otherY = other.y * s + other.yo

      val t = Settings.overlapThreshold

      (math.abs(selfX - otherX) < t) &&
      (math.abs(selfY - otherY) < t)
    }
  }

}
