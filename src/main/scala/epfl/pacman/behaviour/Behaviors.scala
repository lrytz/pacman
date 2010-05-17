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
     directions
  } sinon {
     directionLoinDesMonstres
  }
}
"""

  abstract class Behavior[T <: Figure] {
    abstract class NextMethod(model: Model, c: T) extends Function0[Option[Direction]] {
      val sModel = new StructuredModel(model)

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
          Condition(() => sModel.minDistBetween(model.pacman.pos, positions(model.monsters)) < 10, () => body)
      }

      def siMonstresLoin(body: => Directions) : Condition = {
          Condition(() => sModel.minDistBetween(model.pacman.pos, positions(model.monsters)) >= 10, () => body)
      }

      def choisirParmis(dirs: Directions) : Directions = dirs

      def directions: Directions = {
        val ahead = nextOpt(c.dir)
        val left  = nextOpt(c.dir.left)
        val right = nextOpt(c.dir.right)
        val back  = nextOpt(c.dir.opposite)

        Directions((ahead :: left :: right :: back :: Nil).collect{ case Some(pos) => pos }.toSet)
      }

      def directionLoinDesMonstres: Directions = {
        randomBestDir(withDistBetween(directions.dirs, Set[Position]() ++ model.monsters.map(_.pos)).sortWith((a, b) => a._2 > b._2))
      }

      def directionVersLesMonstres: Directions = {
        randomBestDir(withDistBetween(directions.dirs, Set[Position]() ++ model.monsters.map(_.pos)).sortWith((a, b) => a._2 < b._2))
      }

      def directionVersPacMan: Directions = {
        randomBestDir(withDistBetween(directions.dirs, Set[Position]() + model.pacman.pos).sortWith((a, b) => a._2 < b._2))
      }

      def directionLoinDePacMan: Directions = {
        randomBestDir(withDistBetween(directions.dirs, Set[Position]() + model.pacman.pos).sortWith((a, b) => a._2 > b._2))
      }

      /**
       * Internal stuff
       */

      def randomBestDir(positions: Seq[(Direction, Int)]): Directions = {
        import scala.util.Random.nextInt

        val best = positions.filter(_._2 == positions.head._2)

        Directions(Set[Direction]() + best(nextInt(best.size))._1)
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
               directions
            } sinon {
               directionLoinDesMonstres
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
  /*
  class BehaviorHelper(model: Model, character: Figure) {
    val structuredModel = new StructuredModel(model)

    def nextOpt(dir: Direction) = {
      val pos = character.pos.nextIn(dir)
      if (model.isWallAt(pos)) None
      else Some(pos, dir)
    }

    def allerVers(d: Direction): (Position, Direction) = {
        nextOpt(d).getOrElse(character.pos, d)
    }

    def directionsPossibles: Array[(Position, Direction)] = {
      val ahead = nextOpt(character.dir)
      val left  = nextOpt(character.dir.left)
      val right = nextOpt(character.dir.right)
      val back  = nextOpt(character.dir.opposite)

      Array(ahead, left, right, back).collect{ case Some(posDir) => posDir }
    }

    def randomBestDir(positions: Seq[(Direction, Int)]): Direction = {
      import scala.util.Random.nextInt

      val best = positions.filter(_._2 == positions.head._2)

      best(nextInt(best.size))._1
    }

    def withDistBetween(positions: Seq[(Position, Direction)], to: Set[Position]): Seq[(Direction, Int)] = {
      positions.map(posDir =>
        (posDir._2, structuredModel.minDistBetween(posDir._1, to))
      )
    }
    def directionLoinDesMonstres: Direction = {
      randomBestDir(withDistBetween(directionsPossibles, Set[Position]() ++ model.monsters.map(_.pos)).sortWith((a, b) => a._2 > b._2))
    }

    def directionVersLesMonstres: Direction = {
      randomBestDir(withDistBetween(directionsPossibles, Set[Position]() ++ model.monsters.map(_.pos)).sortWith((a, b) => a._2 < b._2))
    }

    def directionVersPacMan: Direction = {
      randomBestDir(withDistBetween(directionsPossibles, Set[Position]() + model.pacman.pos).sortWith((a, b) => a._2 < b._2))
    }

    def directionLoinDePacMan: Direction = {
      randomBestDir(withDistBetween(directionsPossibles, Set[Position]() + model.pacman.pos).sortWith((a, b) => a._2 > b._2))
    }

  }
  */

  class StructuredModel(model: Model) {
    // all viable blocks
    val allPos = (for (x <- 0 to (Settings.hBlocks-1); y <- 0 to (Settings.vBlocks-1)) yield BlockPosition(x, y)).toSet -- model.wallCache

    def expandPos(pos: Position) : Set[Position] =
      ((-1, 0) :: (1, 0) :: (0, -1) :: (0, 1) :: Nil).map(offset => BlockPosition(pos.x+offset._1, pos.y+offset._2)).toSet + pos

    def expand(poss: Set[Position]) : Set[Position] =
      poss.flatMap(expandPos _) & allPos

    def minDistBetween(from: Position, to: Position): Int =
        minDistBetween(Set(from), Set(to))

    def minDistBetween(from: Position, to: Set[Position]): Int =
        minDistBetween(Set(from), to)

    def minDistBetween(from: Set[Position], to: Set[Position]): Int = {
      var positions = from
      var dist = 0

      while((positions& to).isEmpty) {
        positions = expand(positions);
        dist += 1
      }

      dist
    }
  }

}
