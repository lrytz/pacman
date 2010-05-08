package epfl.pacman.maze.model


object Default {
    val monsters: Set[Monster] = {
        Set() +
        Monster(new Position(1,1), Right) +
        Monster(new Position(28,1), Left) +
        Monster(new Position(28,18), Left) +
        Monster(new Position(1,18), Right)

    }
    val maze: Set[Wall] = {
        def w(x: Int, y: Int) = Wall(new Position(x,y))

        // Default maze, *ouch*
        Set() ++
        (for(x <- 0 to 29)                  yield w(x, 0)) ++
        (for(x <- 0 to 29)                  yield w(x, 19)) ++
        (for(y <- 1 to 6)                   yield w(0, y)) ++
        (for(y <- 1 to 6)                   yield w(29, y)) ++
        (for(y <- 12 to 18)                 yield w(0, y)) ++
        (for(y <- 12 to 18)                 yield w(29, y)) ++
        (for(y <- 7 to 8;   x <- 0 to 5)    yield w(x, y)) ++
        (for(y <- 10 to 11; x <- 0 to 5)    yield w(x, y)) ++
        (for(y <- 7 to 8;   x <- 24 to 29)  yield w(x, y)) ++
        (for(y <- 10 to 11; x <- 24 to 29)  yield w(x, y)) ++
        (for(y <- 2 to 3;   x <- 2 to 5)    yield w(x, y)) ++
        (for(y <- 2 to 3;   x <- 24 to 27)  yield w(x, y)) ++
        (for(y <- 2 to 3;   x <- 7 to 11)   yield w(x, y)) ++
        (for(y <- 2 to 3;   x <- 18 to 22)  yield w(x, y)) ++
        (for(y <- 5 to 5;   x <- 2 to 5)    yield w(x, y)) ++
        (for(y <- 5 to 5;   x <- 24 to 27)  yield w(x, y)) ++
        (for(y <- 5 to 6;   x <- 10 to 19)  yield w(x, y)) ++
        (for(y <- 8 to 9;   x <- 13 to 16)  yield w(x, y)) ++
        (for(y <- 1 to 3;   x <- 13 to 16)  yield w(x, y)) ++
        (for(y <- 5 to 13;  x <- 7 to 8)    yield w(x, y)) ++
        (for(y <- 5 to 13;  x <- 21 to 22)  yield w(x, y)) ++
        (for(y <- 8 to 9;   x <- 18 to 20)  yield w(x, y)) ++
        (for(y <- 8 to 9;   x <- 8 to 11)   yield w(x, y)) ++
        (for(y <- 11 to 17; x <- 19 to 19)  yield w(x, y)) ++
        (for(y <- 11 to 11; x <- 11 to 13)  yield w(x, y)) ++
        (for(y <- 11 to 11; x <- 15 to 18)  yield w(x, y)) ++
        (for(y <- 17 to 17; x <- 11 to 14)  yield w(x, y)) ++
        (for(y <- 17 to 17; x <- 16 to 18)  yield w(x, y)) ++
        (for(y <- 17 to 17; x <- 16 to 18)  yield w(x, y)) ++
        (for(y <- 13 to 13; x <- 12 to 17)  yield w(x, y)) ++
        (for(y <- 15 to 15; x <- 12 to 17)  yield w(x, y)) ++
        (for(y <- 11 to 17; x <- 10 to 10)  yield w(x, y)) ++
        (for(y <- 13 to 13; x <- 2 to 5)    yield w(x, y)) ++
        (for(y <- 15 to 15; x <- 1 to 3)    yield w(x, y)) ++
        (for(y <- 15 to 15; x <- 26 to 28)  yield w(x, y)) ++
        (for(y <- 13 to 13; x <- 24 to 27)  yield w(x, y)) ++
        (for(y <- 15 to 17; x <- 7 to 8)    yield w(x, y)) ++
        (for(y <- 15 to 17; x <- 5 to 5)    yield w(x, y)) ++
        (for(y <- 15 to 17; x <- 24 to 24)  yield w(x, y)) ++
        (for(y <- 17 to 17; x <- 2 to 4)    yield w(x, y)) ++
        (for(y <- 17 to 17; x <- 25 to 27)  yield w(x, y)) ++
        (for(y <- 15 to 17; x <- 21 to 22)  yield w(x, y))
    }
}

case class Model(val pacman: Pacman, val monsters: Set[Monster], val walls: Set[Wall]) {

    val objectsByPos = {
        var map = Map[Position, Object]() + (pacman.pos -> pacman)

        for (m <- monsters) map += m.pos -> m

        for (w <- walls) map += w.pos -> w

        if (map.size != monsters.size + walls.size + 1) {
            throw new Exception("Collisions in board")
        }

        map
    }

    def this() = this(new Pacman(new Position(14, 10), Right), Default.monsters, Default.maze)


    def isWallAt(pos: Position) = objectsByPos.get(pos) match {
        case Some(_: Wall) => true
        case _ => false
    }
}
