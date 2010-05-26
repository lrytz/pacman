package epfl.pacman
package behaviour

import scala.util.Random.{nextInt => randomInt}

import maze.MVC

object Behavior {
  def defaultBehavior =
"""siChasseur {
  bouger telQue versLesMonstres
} sinon {
  siMonstresLoin {
    bouger telQue versUnPoint
  } sinon {
    bouger telQue loinDesMonstres
  }
}
"""
}

abstract class Behavior {
  val mvc: MVC
  import mvc._

  type Filter = Directions => Directions

  abstract class NextMethod(model: Model, c: Figure) extends Function0[Option[Direction]] {

    def apply: Option[Direction]

    /**
     * Exposed DSL
     */
    def si(cond: => Boolean)(body: => Directions): Condition = {
        Condition(() => cond, () => body)
    }

    def alterner(weight: Int) =
        si(weight > randomInt(100)) _

    def auHasard = alterner(50)

    def siChasseur = si(model.pacman.hunter) _
    def siChassé   = si(!model.pacman.hunter) _

    def siMonstresPrès = si(model.minDistBetween(model.pacman.pos, model.pacman.pos, positions(model.monsters)) <  6) _
    def siMonstresLoin = si(model.minDistBetween(model.pacman.pos, model.pacman.pos, positions(model.monsters)) >= 6) _

    def choisirParmis(dirs: Directions) : Directions = dirs

    def directions: Directions = {
      val ahead = nextOpt(c.dir)
      val left  = nextOpt(c.dir.left)
      val right = nextOpt(c.dir.right)
      val back  = nextOpt(c.dir.opposite)

      Directions((ahead :: left :: right :: back :: Nil).collect{ case Some(pos) => pos }.toSet)
    }

    private def distanceTo(to: Set[Position]): Int = {
        withDistBetween(directions.dirs, to).sortWith((a, b) => a._2 < b._2).head._2
    }

    def distanceVersCerise =
        distanceTo(Set[Position]() ++ model.points.collect{ case SuperPoint(pos) => pos })

    def distanceVersPoint =
        distanceTo(Set[Position]() ++ model.points.map(_.pos))

    def distanceVersMonstre =
        distanceTo(Set[Position]() ++ model.monsters.map(_.pos))

    val Bouger = directions
    val bouger = directions

    val Rester = NoDirections

    def directionsEnAvant: Directions = {
      val ahead = nextOpt(c.dir)
      val left  = nextOpt(c.dir.left)
      val right = nextOpt(c.dir.right)

      Directions((ahead :: left :: right :: Nil).collect{ case Some(pos) => pos }.toSet)
    }

    def enAvant(ds: Directions): Directions = {
      Directions(directionsEnAvant.dirs & ds.dirs)
    }

    def loinDesMonstres(ds: Directions): Directions = {
      maxWeightedDistToVia(Set[Position]() ++ model.monsters.map(_.pos), ds, directionsEnAvant, 5)
    }

    def versLesMonstres(ds: Directions): Directions = {
      minDistToVia(Set[Position]() ++ model.monsters.map(_.pos), ds)
    }

    def loinDesPoints(ds: Directions): Directions = {
      maxDistToVia(Set[Position]() ++ model.points.map(_.pos), ds)
    }

    def versUnPoint(ds: Directions): Directions = {
      minDistToVia(Set[Position]() ++ model.points.map(_.pos), ds)
    }

    def loinDesCerises(ds: Directions): Directions = {
      maxDistToVia(Set[Position]() ++ model.points.collect{ case SuperPoint(pos) => pos }, ds)
    }

    def versUneCerise(ds: Directions): Directions = {
      minDistToVia(Set[Position]() ++ model.points.collect{ case SuperPoint(pos) => pos }, ds)
    }

    def versPacMan(ds: Directions): Directions = {
      minDistToVia(Set[Position](model.pacman.pos), ds)
    }

    def loinDePacMan(ds: Directions): Directions = {
      maxDistToVia(Set[Position](model.pacman.pos), ds)
    }

    def pasMourir(n: Int)(ds: Directions): Directions = {
      println("##################");
      val dirDists = ds.dirs.map(d => (d, model.maxSafePathBetween(model.pacman.pos, d, Set[Position]() ++ model.monsters.map(_.pos), n+1)))
      println(dirDists);
      val okDists = dirDists.filter(d => d._2 > n).toSeq

      if (okDists.size > 0) {
        // Return only the direction with top safe path
        Directions(Set[Direction]() + okDists.sortWith((a,b) => a._2 > b._2).head._1)
      } else {
        NoDirections
      }
    }

    val Droite = new Directions(Set(Right))
    val Gauche = new Directions(Set(Left))
    val Bas    = new Directions(Set(Down))
    val Haut   = new Directions(Set(Up))

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

    def minDistToVia(to: Set[Position], ds: Directions) = {
      randomBestDir(withDistBetween(ds.dirs, to).sortWith((a, b) => a._2 < b._2))
    }

    def maxDistToVia(to: Set[Position], ds: Directions) = {
      randomBestDir(withDistBetween(ds.dirs, to).sortWith((a, b) => a._2 > b._2))
    }

    def maxWeightedDistToVia(to: Set[Position], ds: Directions, weightedDirs: Directions, weight: Int) = {
      randomBestDir(withDistBetween(ds.dirs, to).map(d => (d._1, if (weightedDirs.dirs contains d._1) d._2+weight else d._2)).sortWith((a, b) => a._2 > b._2))
    }

    def maxPathToVia(to: Set[Position], ds: Directions) = {
      randomBestDir(withPathBetween(ds.dirs, to).sortWith((a, b) => a._2 > b._2))
    }

    def randomBestDir(positions: Seq[(Direction, Int)]): Directions = {
      val best = positions.filter(_._2 == positions.head._2)

      Directions(Set[Direction]() + best(randomInt(best.size))._1)
    }

    def withPathBetween(directions: Set[Direction], to: Set[Position]): Seq[(Direction, Int)] = {
      directions.toSeq.map(dir =>
        (dir, model.maxPathBetween(c.pos, dir, to))
      )
    }

    def withDistBetween(directions: Set[Direction], to: Set[Position]): Seq[(Direction, Int)] = {
      directions.toSeq.map(dir =>
        (dir, model.minDistBetween(c.pos, c.pos.nextIn(dir), to))
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
            elze.map(el => el()).getOrElse(error("No else!"))
        }
    }
  }

  case class Directions(dirs: Set[Direction]) {

     def tellesQue(cond: Filter) = {
        cond(this)
     }

     def telQue = tellesQue _

     def telque(cond: Filter) = {
        cond(this)
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
          bouger telQue versLesMonstres
        } sinon {
          siMonstresLoin {
            bouger telQue versUnPoint
          } sinon {
            bouger telQue loinDesMonstres
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
          bouger telQue loinDePacMan
        } sinon {
          auHasard {
              bouger telQue versPacMan
          } sinon {
              bouger telQue enAvant
          }
        }
      }
    }
  }
}
