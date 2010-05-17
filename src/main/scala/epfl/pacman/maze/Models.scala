package epfl.pacman
package maze

trait Models { this: MVC =>

  var model: Model

  case class Model(pacman: PacMan, monsters: Set[Monster], walls: Set[Wall], points: Set[Point], paused: Boolean) {

    def this() = this(new PacMan(new OffsetPosition(14, 10), Right, Hunted, Angle(30)), ModelDefaults.monsters, ModelDefaults.maze, ModelDefaults.points, false)

    def randomizeFigures() = {
      import scala.util.Random.nextInt
      def getPos(avoid: Set[Monster]): OffsetPosition = {
        var p = OffsetPosition(0, 0)
        do {
          p = new OffsetPosition(nextInt(Settings.hBlocks), nextInt(Settings.vBlocks))
        } while (isWallAt(p) || avoid.exists(m => m.pos == p))
        p
      }
      val newMonsters = monsters map (m => m.copy(getPos(Set())))
      copy(pacman.copy(getPos(newMonsters)), newMonsters)
    }


    val wallCache = Set[BlockPosition]() ++ walls.map(_.pos)

    def isWallAt(pos: Position) = wallCache.contains(pos) // walls.exists(_.pos == pos)

    def clearPathBetween(from: Figure, to: Figure) = {
        // check whether <from> can "see" <to>
        if (from.pos.x == to.pos.x) {
            (from.pos.y.min(to.pos.y) to from.pos.y.max(to.pos.y)).forall(y =>
                !isWallAt(BlockPosition(from.pos.x, y))
            )
        } else if (from.pos.y == to.pos.y) {
            (from.pos.x.min(to.pos.x) to from.pos.x.max(to.pos.x)).forall(x =>
                !isWallAt(BlockPosition(x, from.pos.y))
            )
        } else {
            false
        }
    }
  }

  object ModelDefaults {
    val monsters: Set[Monster] = {
      Set() + Monster(new OffsetPosition(1,1),  Right, new LaserSettings(true, 0)) +
              Monster(new OffsetPosition(28,1),  Left, new LaserSettings(true, 0)) +
              Monster(new OffsetPosition(28,18), Left, new LaserSettings(true, 0)) +
              Monster(new OffsetPosition(1,18), Right, new LaserSettings(true, 0))

    }

    val maze: Set[Wall] = {
      def w(x: Int, y: Int) = Wall(new BlockPosition(x,y))

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

    val points: Set[Point] = {
      import scala.util.Random.nextInt
      val wallsPos = Set[Position]() ++ maze.map(w => w.pos)

      collection.immutable.ListSet[Point]() ++
        (for (x <- 0 to 30; y <- 0 to 20 if !(wallsPos contains BlockPosition(x, y))) yield {
          if (nextInt(10) == 0) {
            SuperPoint(new BlockPosition(x, y))
          } else {
            NormalPoint(new BlockPosition(x, y))
          }
        })
    }
  }
}
