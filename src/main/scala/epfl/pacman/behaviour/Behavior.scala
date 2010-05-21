package epfl.pacman
package behaviour

import maze.MVC

object Behavior {
  def defaultBehavior =
"""siChasseur {
  directionVersLesMonstres
} sinon {
  siMonstresLoin {
    directionLoinDesMonstres
  } sinon {
    cheminLoinDesMonstres
  }
}"""
}

abstract class Behavior {
  val mvc: MVC
  import mvc._

  abstract class NextMethod(model: Model, c: Figure) extends Function0[Option[Direction]] {

    def apply: Option[Direction]

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
        Condition(() => model.minDistBetween(model.pacman.pos, positions(model.monsters)) < 6, () => body)
    }

    def siMonstresLoin(body: => Directions) : Condition = {
        Condition(() => model.minDistBetween(model.pacman.pos, positions(model.monsters)) >= 6, () => body)
    }

    def choisirParmis(dirs: Directions) : Directions = dirs

    def retourne: Directions = Directions(Set(c.dir.opposite))

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

    val Rester = new Directions(Set())


    /**
     * @TODO: add to dsl
     * - random (avecProbabilité)
     * - versPoint
     * - versCerise
     * - choisirAleatoire
     */


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
        (dir, model.maxPathBetween(c.pos, dir, to))
      )
    }

    def withDistBetween(directions: Set[Direction], to: Set[Position]): Seq[(Direction, Int)] = {
      directions.toSeq.map(dir =>
        (dir, model.minDistBetween(c.pos.nextIn(dir), to))
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

  def getMethod(model: Model, c: Figure): () => Option[Direction]

  def next(model: Model, c: Figure) = getMethod(model, c)()



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

  case class Directions(dirs: Set[Direction]) {
     def telQue(cond: Direction => Boolean) = tellesQue(cond)

     def tellesQue(cond: Direction => Boolean) = {
        Directions(dirs filter cond)
     }

     def ouAlors(body: => Directions) = {
        Condition(() => !dirs.isEmpty, () => this, Some(() => body))
     }
  }

  object NoDirections extends Directions(Set())



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

}

abstract class DefaultPacManBehavior extends Behavior {
  import mvc._
  def getMethod(model: Model, p: Figure) = {
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

abstract class DefaultMonsterBehavior extends Behavior {
  import mvc._
  def getMethod(model: Model, p: Figure) = {
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
