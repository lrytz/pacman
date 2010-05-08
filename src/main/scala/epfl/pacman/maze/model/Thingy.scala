package epfl.pacman.maze.model

abstract class Thingy(val pos: Position)

case class Figure(override val pos: OffsetPosition, val dir: Direction) extends Thingy(pos) {
  def incrOffset() {
    dir match {
      case Up    => pos.yo -= 1
      case Left  => pos.xo -= 1
      case Down  => pos.yo += 1
      case Right => pos.xo += 1
    }
  }
}

case class Wall(override val pos: Position) extends Thingy(pos)
