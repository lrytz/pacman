package epfl.pacman
package maze

import collection.{mutable => mut, immutable => imm}

trait Models extends Thingies with Positions with Directions { this: MVC =>
  
  case class Model(pacman: PacMan = ModelDefaults.pacman,
                   monsters: Set[Monster] = ModelDefaults.monsters,
                   walls: Set[Wall] = ModelDefaults.maze,
                   points: Set[Thingy] = ModelDefaults.points,
                   paused: Boolean = false,
                   simpleMode: Boolean = true,
                   deadMonsters: Set[Monster] = Set(),
                   counters: Counters = new Counters(),
                   message: String = "Jeu en pause...") {

    def resetFigures() = {
      counters.clear()
      copy(pacman = ModelDefaults.pacman.copy(lives = pacman.lives),
           monsters = ModelDefaults.monsters, deadMonsters = Set())
    }

    private val wallCache = Set[BlockPosition]() ++ walls.map(_.pos)
    def isWallAt(pos: Position) = wallCache.contains(pos)


    // all viable blocks
    private val allPos = (for (x <- 0 to (Settings.hBlocks-1); y <- 0 to (Settings.vBlocks-1)) yield BlockPosition(x, y)).toSet -- wallCache
    private val g = new Graph

    // set up graph: add all nodes
    for (p <- allPos) {
      g.addNode(p)
    }

    // set up graph: connect nodes
    for (fromP <- allPos) {
      for (toD <- List(Left, Right, Up, Down)) {
        var toP = fromP.nextIn(toD)

        if (!isWallAt(toP)) {
          g.addEdge(fromP, toP)
        }
      }
    }

    /**
     * ???
     */
    def randomValidPos = {
      import scala.util.Random.nextInt
      allPos.toSeq.apply(nextInt(allPos.size))
    }

    def minDistBetween(init: Position, from: Position, to: Position): Int =
      minDistBetween(init, from, Set(to))

    /**
     * ???
     */
    def minDistBetween(init: Position, from: Position, to: Set[Position]): Int = {
      g.markTargets(to)
      g.mark(init)
      val r = g.simpleDistFrom(from, 45)
      g.clear
      r
    }

    /**
     * ???
     */
    def maxPathBetween(init: Position, dir: Direction, to: Set[Position]): Int = {
      g.markTargets(to + init)
      val r = g.maxPathFrom(init.nextIn(dir), 45)
      g.clear
      r
    }

    private class Graph {
      case class Node(pos: Position, var color: Int = 0)
      var nodes     = imm.Map[Position, Node]()
      var edgesFrom = imm.Map[Node, Set[Node]]().withDefaultValue(Set())

      def addNode(pos: Position) {
        if (!(nodes contains pos)) {
          nodes += (pos -> Node(pos))
        } else {
          error("Node "+pos+" already in")
        }
      }
      def addEdge(from: Position, to: Position) {
        val fromN = nodes(from)
        val toN   = nodes(to)
        edgesFrom += fromN -> (edgesFrom(fromN) + toN)
      }

      def markTargets(positions: Set[Position]) {
        for (p <- positions) {
          nodes(p).color = 2
        }
      }

      def mark(pos: Position) {
        nodes(pos).color = 1
      }

      def clear {
        for ((p, n) <- nodes) {
          n.color = 0
        }
      }

      def simpleDistFrom(p: Position, max: Int): Int = {
        var toVisit: Set[Node] = Set(nodes(p))
        var dist = 0

        while (!toVisit.isEmpty && dist < max) {
          dist += 1
          val toVisitBatch = toVisit
          for (n <- toVisitBatch) {
            if (n.color == 0) {
              edgesFrom(n).foreach(toVisit += _)
              n.color = 1;
            } else if (n.color == 2) {
              return dist;
            }
          }
        }

        dist min max
      }

      def maxPathFrom(p: Position, max: Int): Int = {
        var toVisit: Set[Node] = Set(nodes(p))
        var dist = 0

        while (!toVisit.isEmpty && dist < max) {
          dist += 1
          val toVisitBatch = toVisit
          for (n <- toVisitBatch) {
            if (n.color == 0) {
              edgesFrom(n).foreach(toVisit += _)
              n.color = 1;
            } else if (n.color == 2) {
              // ignore this path
            }
          }
        }

        dist min max
      }
    }

  }

  class Counters extends mut.HashMap[Any, Int] {
    override def default(k: Any) = {
      if (k == 'time) Settings.surviveTime
      else 0
    }
  }

  object ModelDefaults {
    val pacman = new PacMan(new OffsetPosition(14, 10), Right)

    val monsters: Set[Monster] = {
      Set() + Monster(new OffsetPosition(1,1), Right) +
              Monster(new OffsetPosition(28,1), Left) +
              Monster(new OffsetPosition(28,18), Left) +
              Monster(new OffsetPosition(1,18), Right)
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

    def points: Set[Thingy] = {
      import scala.util.Random.nextInt
      val wallsPos = Set[Position]() ++ maze.map(w => w.pos)

      collection.immutable.ListSet[Thingy]() ++
              (for (x <- 0 to 29; y <- 0 to 19 if !(wallsPos contains BlockPosition(x, y))) yield {
                if (nextInt(100) < Settings.superPointsRatio) {
                  SuperPoint(new BlockPosition(x, y))
                } else {
                  NormalPoint(new BlockPosition(x, y))
                }
              })
    }
  }
}
