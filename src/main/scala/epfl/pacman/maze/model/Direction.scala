package epfl.pacman.maze.model

sealed abstract class Direction

case object Left  extends Direction
case object Right extends Direction
case object Up    extends Direction
case object Down  extends Direction
