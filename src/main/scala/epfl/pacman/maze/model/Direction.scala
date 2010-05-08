package epfl.pacman.maze

package model {
  /**
   * 0 Up
   * 1 Left
   * 2 Down
   * 3 Right
   */
  case class Direction(d: Int) {
    def left      = Direction((d + 1) % 4)
    def right     = Direction((d + 3) % 4)
    def opposite  = Direction((d + 2) % 4)

    override def toString() = (d: @unchecked) match {
      case 0 => "Up"
      case 1 => "Left"
      case 2 => "Down"
      case 3 => "Right"
    }
  }
}

package object model {
  val Up    = Direction(0)
  val Left  = Direction(1)
  val Down  = Direction(2)
  val Right = Direction(3)
}
