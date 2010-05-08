package epfl.pacman.model

case class Pacman(p: Position, d: Direction) extends MovingObject(p, d) {
    def move(p: Position) = {
        if (((pos.x-p.x).abs + (pos.y-p.y).abs) != 1) {
            throw new Exception("Non adjacent move position")
        }

        Pacman(p, d)
    }
}
