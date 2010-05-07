package scala.epfl.pacman.model

sealed class Position(val x: Int, val y: Int) {
    override def toString = "("+x+","+y+")"

    def onTop    = new Position(x, y-1)
    def onBottom = new Position(x, y+1)
    def onLeft   = new Position(x-1, y)
    def onRight  = new Position(x+1, y)
    
    override def hashCode = x + y

    override def equals(v: Any) = (this, v) match {
        case (t: HighResPosition, v: HighResPosition) =>
            t.x == v.x && t.y == v.y &&
            t.offsetx == v.offsetx && t.offsety == v.offsety
        case (t: HighResPosition, v: Position) =>
            t.x == v.x && t.y == v.y &&
            t.offsetx == 0 && t.offsety == 0
        case (t: Position, v: HighResPosition) =>
            t.x == v.x && t.y == v.y &&
            0 == v.offsetx && 0 == v.offsety
        case (t: Position, v: Position) =>
            t.x == v.x && t.y == v.y
        case _ => false
    }

}

class HighResPosition(override val x: Int, override val y: Int, val offsetx: Int, val offsety: Int) extends Position(x, y)
