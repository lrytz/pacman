package scala.epfl.pacman.model

sealed case class Position(x: Int, y: Int) {
    override def toString = "("+x+","+y+")"


    def onTop    = Position(x, y-1)
    def onBottom = Position(x, y+1)
    def onLeft   = Position(x-1, y)
    def onRight  = Position(x+1, y)
}
