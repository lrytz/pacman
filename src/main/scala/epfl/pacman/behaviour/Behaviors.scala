package epfl.pacman
package behaviour

import maze.MVC

trait Behaviors { this: MVC =>

  def defaultBehavior =
"""
siChasseur {
  directionVersLesMonstres
} sinon {
  siMonstresLoin {
     directionLoinDesMonstres
  } sinon {
     cheminLoinDesMonstres
  }
}
"""

  abstract class Behavior[T <: Figure] {
    val sModel = new StructuredModel(new Model)

    abstract class NextMethod(model: Model, c: T) extends Function0[Option[Direction]] {

      def apply: Option[Direction];

      /**
       * Exposed DSL
       */
      def siChasseur(body: => Directions): Condition = {
          Condition(() => model.pacman.mode == Hunter, () => body)
      }

      def siChassé(body: => Directions): Condition = {
          Condition(() => model.pacman.mode == Hunted, () => body)
      }

      def siMonstresPrès(body: => Directions) : Condition = {
          Condition(() => sModel.minDistBetween(model.pacman.pos, positions(model.monsters)) < 20, () => body)
      }

      def siMonstresLoin(body: => Directions) : Condition = {
          Condition(() => sModel.minDistBetween(model.pacman.pos, positions(model.monsters)) >= 20, () => body)
      }

      def choisirParmis(dirs: Directions) : Directions = dirs

      def directions: Directions = {
        val ahead = nextOpt(c.dir)
        val left  = nextOpt(c.dir.left)
        val right = nextOpt(c.dir.right)
        val back  = nextOpt(c.dir.opposite)

        Directions((ahead :: left :: right :: back :: Nil).collect{ case Some(pos) => pos }.toSet)
      }

      def directionsEnAvant: Directions = {
        val ahead = nextOpt(c.dir)
        val left  = nextOpt(c.dir.left)
        val right = nextOpt(c.dir.right)

        Directions((ahead :: left :: right :: Nil).collect{ case Some(pos) => pos }.toSet)
      }

      def directionLoinDesMonstres: Directions = {
        maxDistTo(Set[Position]() ++ model.monsters.map(_.pos))
      }

      def cheminLoinDesMonstres: Directions = {
        maxPathTo(Set[Position]() ++ model.monsters.map(_.pos))
      }

      def directionVersLesMonstres: Directions = {
        minDistTo(Set[Position]() ++ model.monsters.map(_.pos))
      }

      def directionVersPacMan: Directions = {
        minDistTo(Set[Position](model.pacman.pos))
      }

      def directionLoinDePacMan: Directions = {
        maxDistTo(Set[Position](model.pacman.pos))
      }

      val Droite = new Directions(Set(Right))
      val Gauche = new Directions(Set(Left))
      val Bas    = new Directions(Set(Down))
      val Haut   = new Directions(Set(Up))

      /**
       * Internal stuff
       */

      def minDistTo(to: Set[Position]) = {
        randomBestDir(withDistBetween(directions.dirs, to).sortWith((a, b) => a._2 < b._2))
      }

      def maxDistTo(to: Set[Position]) = {
        randomBestDir(withDistBetween(directions.dirs, to).sortWith((a, b) => a._2 > b._2))
      }

      def maxPathTo(to: Set[Position]) = {
        randomBestDir(withPathBetween(directionsEnAvant.dirs, to).sortWith((a, b) => a._2 > b._2))
      }

      def randomBestDir(positions: Seq[(Direction, Int)]): Directions = {
        import scala.util.Random.nextInt

        val best = positions.filter(_._2 == positions.head._2)

        Directions(Set[Direction]() + best(nextInt(best.size))._1)
      }

      def withPathBetween(directions: Set[Direction], to: Set[Position]): Seq[(Direction, Int)] = {
        directions.toSeq.map(dir =>
          (dir, sModel.maxPathBetween(c.pos, dir, to))
        )
      }

      def withDistBetween(directions: Set[Direction], to: Set[Position]): Seq[(Direction, Int)] = {
        directions.toSeq.map(dir =>
          (dir, sModel.minDistBetween(c.pos.nextIn(dir), to))
        )
      }

      def nextOpt(dir: Direction): Option[Direction] = {
        val pos = c.pos.nextIn(dir)
        if (model.isWallAt(pos)) None
        else Some(dir)
      }

      def positions(figures: Set[Monster]): Set[Position] =
        Set[Position]() ++ figures.map(_.pos)

    }

    def getMethod(model: Model, c: T): () => Option[Direction];

    def next(model: Model, c: T) = getMethod(model, c)()
  }

  case class Condition(cond: () => Boolean, then: () => Directions, elze: Option[() => Directions] = None) {
    def sinon(body: => Directions) = Condition(cond, then, Some(() => body))

    def value: Directions = {
        if(cond()) {
            then()
        } else {
            elze.map(el => el()).getOrElse(NoDirections)
        }
    }
  }

  /**
   * Some implicit magic to make it all work smoothly
   */
  implicit def conditionsToDirections(c: Condition): Directions = c.value

  implicit def conditionsToOptDirection(c: Condition): Option[Direction] = directionsToOptDirection(c.value)

  implicit def directionsToOptDirection(dirs: Directions): Option[Direction] = {
    if (dirs.dirs.isEmpty) {
      None
    } else {
      Some(dirs.dirs.head)
    }
  }


  case class Directions(dirs: Set[Direction]) {
     def tellesQue(cond: Direction => Boolean) = {
        Directions(dirs filter cond)
     }

     def ouAlors(body: => Directions) = {
        Condition(() => !dirs.isEmpty, () => this, Some(() => body))
     }
  }

  object NoDirections extends Directions(Set())

  /* Default implementation of various behaviors */
  abstract class PacManBehavior extends Behavior[PacMan]
  abstract class MonsterBehavior extends Behavior[Monster]

  class DefaultPacManBehavior extends PacManBehavior {
    def getMethod(model: Model, p: PacMan) = {
      new NextMethod(model, p) {
        def apply = {
          siChasseur {
            directionVersLesMonstres
          } sinon {
            siMonstresLoin {
               directionLoinDesMonstres
            } sinon {
               cheminLoinDesMonstres
            }
          }
        }
      }
    }
  }

  class DefaultMonsterBehavior extends MonsterBehavior{
    def getMethod(model: Model, p: Monster) = {
      new NextMethod(model, p) {
        def apply = {
          siChasseur {
            directionLoinDePacMan
          } sinon {
            directionVersPacMan
          }
        }
      }
    }
  }

    /**
     * Conditions:
     *
     * monstresLoin
     * monstresPres
     * chasseur
     * chassé
     *
     *
     * Directions:
     * 
     * versLesMonstres
     * loinDesMonstres
     * versLesPoints
     * loinDesPoints
     * versLesCerises
     * loinDesCerises
     * 
     */

  class StructuredModel(model: Model) {
    // Build a undirected graph of the possible ways
    class Graph {
      case class Node(pos: Position, var color: Int = 0)
      var nodes     = Map[Position, Node]()
      var edgesFrom = Map[Node, Set[Node]]().withDefaultValue(Set())

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

    // all viable blocks
    val allPos = (for (x <- 0 to (Settings.hBlocks-1); y <- 0 to (Settings.vBlocks-1)) yield BlockPosition(x, y)).toSet -- model.wallCache
    val g = new Graph

    // add all nodes
    for (p <- allPos) {
      g.addNode(p)
    }
    // connect them
    for (fromP <- allPos) {
      for (toD <- List(Left, Right, Up, Down)) {
        var toP = fromP.nextIn(toD)

        if (!model.isWallAt(toP)) {
          g.addEdge(fromP, toP)
        }
      }
    }

    def randomValidPos = {
        import scala.util.Random.nextInt
        allPos.toSeq.apply(nextInt(allPos.size))
    }

    def minDistBetween(from: Position, to: Position): Int =
      minDistBetween(from, Set(to))

    def minDistBetween(from: Position, to: Set[Position]): Int = {
      g.markTargets(to)
      val r = g.simpleDistFrom(from, 45)
      g.clear
      r
    }

    def maxPathBetween(init: Position, dir: Direction, to: Set[Position]): Int = {
      g.markTargets(to + init)
      val r = g.maxPathFrom(init.nextIn(dir), 45)
      g.clear
      r
    }
  }
}
