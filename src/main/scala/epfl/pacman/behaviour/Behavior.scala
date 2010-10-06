package epfl.pacman
package behaviour

import scala.util.Random.{nextInt => randomInt}

import maze.MVC

abstract class Behavior {
  val mvc: MVC
  import mvc._

  type Filter = Directions => Directions

  abstract class NextMethod(model: Model, c: Figure) extends Function0[Option[Direction]] {

    def apply: Option[Direction]

    /**
     * Exposed DSL
     */
    val bouge = directions
    val Bouge = directions

    val reste = NoDirections
    val Reste = NoDirections


    // CONDITIONS

    def si(cond: => Boolean)(body: => Directions): Condition = {
        Condition(() => cond, () => body)
    }

    def siChasseur = si(model.pacman.hunter) _
    def siChassé   = si(!model.pacman.hunter) _
    def siChasse   = siChassé

    def siMonstrePrès = si(model.minDistBetween(model.pacman.pos, model.pacman.pos, positions(model.monsters), Set[Position]()) <  Settings.farDistance) _
    def siMonstresPrès = siMonstrePrès
    def siMonstrePres = siMonstrePrès
    def siMonstresPres = siMonstrePrès

    def siMonstresLoin = si(model.minDistBetween(model.pacman.pos, model.pacman.pos, positions(model.monsters), Set[Position]()) >= Settings.farDistance) _

    def siMonstreLoin = siMonstresLoin

    def alterner(weight: Int) =
        si(weight < randomInt(100)) _

    def auHasard = alterner(50)


    // DISTANCES

    def distanceVersCerise =
        distanceTo(Set[Position]() ++ model.points.collect{ case SuperPoint(pos) => pos })

    def distanceVersPoint =
        distanceTo(Set[Position]() ++ model.points.map(_.pos))

    def distanceVersMonstre =
        distanceTo(Set[Position]() ++ model.monsters.map(_.pos))


    // FILTERS

    def àDroite(ds: Directions): Directions =
      Directions(ds.dirs & Set(Right))
    def aDroite(ds: Directions): Directions = àDroite(ds)

    def àGauche(ds: Directions): Directions =
      Directions(ds.dirs & Set(Left))
    def aGauche(ds: Directions): Directions = àGauche(ds)

    def enHaut(ds: Directions): Directions =
      Directions(ds.dirs & Set(Up))

    def enBas(ds: Directions): Directions =
      Directions(ds.dirs & Set(Down))

    def enAvant(ds: Directions): Directions = {
      Directions(directionsEnAvant.dirs & ds.dirs)
    }

    def versPos(p: Position)(ds: Directions): Directions = {
        if (p == model.pacman.pos) {
          NoDirections
        } else {
          minDistToVia(Set(p), ds)
        }
    }

    def coinHautGauche = versPos(BlockPosition(1, 1)) _
    def coinHautDroite = versPos(BlockPosition(Settings.hBlocks-2, 1)) _
    def coinBasDroite  = versPos(BlockPosition(Settings.hBlocks-2, Settings.vBlocks-2)) _
    def coinBasGauche  = versPos(BlockPosition(1, Settings.vBlocks-2)) _

    def versUnMonstre(ds: Directions): Directions =
      minDistToVia(Set[Position]() ++ model.monsters.map(_.pos), ds)

    def loinDesMonstres(ds: Directions): Directions =
      maxWeightedDistToVia(Set[Position]() ++ model.monsters.map(_.pos), ds, directionsEnAvant, 5)

    def versUnPoint(ds: Directions): Directions =
      if (model.points.isEmpty) {
        NoDirections
      } else {
        minDistToVia(Set[Position]() ++ model.points.map(_.pos), ds)
      }

    def loinDesPoints(ds: Directions): Directions =
      if (model.points.isEmpty) {
        NoDirections
      } else {
        maxDistToVia(Set[Position]() ++ model.points.map(_.pos), ds)
      }

    def versUneCerise(ds: Directions): Directions =
      if (!model.points.exists(p => p.isInstanceOf[SuperPoint])) {
        NoDirections
      } else {
        minDistToVia(Set[Position]() ++ model.points.collect{ case SuperPoint(pos) => pos }, ds)
      }

    def loinDesCerises(ds: Directions): Directions =
      if (!model.points.exists(p => p.isInstanceOf[SuperPoint])) {
        NoDirections
      } else {
        maxDistToVia(Set[Position]() ++ model.points.collect{ case SuperPoint(pos) => pos }, ds)
      }
 
    def versPacMan(ds: Directions): Directions =
      minDistToVia(Set[Position](model.pacman.pos), ds)

    def loinDePacMan(ds: Directions): Directions =
      maxDistToVia(Set[Position](model.pacman.pos), ds)

    def versPacManSansMonstreSurLeChemin(ds: Directions): Directions = {
      val withDists = ds.dirs.toSeq.map(dir =>
        (dir, model.minDistBetween(c.pos, c.pos.nextIn(dir), model.pacman.pos, Set[Position]() ++ model.monsters.collect{ case m /*if m != c*/ => m.pos }))
      )

      randomBestDir(withDists.sortWith((a, b) => a._2 < b._2))
    }

    def enSécuritéPendant(n: Int)(ds: Directions): Directions = {
      val dirDists = ds.dirs.map(d => (d, model.maxSafePathBetween(model.pacman.pos, d, Set[Position]() ++ model.monsters.map(_.pos), n+1)))
      val okDists = dirDists.filter(d => d._2 > n).toSeq

      if (okDists.size > 0) {
        // Return only the direction with top safe path
        Directions(Set[Direction]() ++ okDists.sortWith((a,b) => a._2 > b._2).map(_._1))
      } else {
        NoDirections
      }
    }
    def enSecuritePendant(n: Int)(ds: Directions) = enSécuritéPendant(n)(ds)



    /**
     * ENGLISH DSL
     */
    val move = bouge
    val Move = bouge
    val stay = reste
    val Stay = reste


    // CONDITIONS

    def when(cond: => Boolean)(body: => Directions): Condition = si(cond)(body)

    def ifHunter = siChasseur
    def ifHunted = siChasse

    def ifMonsterClose  = siMonstrePres
    def ifMonstersClose = siMonstrePres
    def ifMonstersFar   = siMonstresLoin
    def ifMonsterFar    = siMonstresLoin

    def alternate(weight: Int) = alterner(weight)

    def randomly = auHasard


    // DISTANCES

    def distanceToCherry = distanceVersCerise
    def distanceToPoint = distanceVersPoint
    def distanceToMonster = distanceVersMonstre


    // FILTERS

    def right(ds: Directions): Directions = aDroite(ds)
    def left(ds: Directions): Directions = aGauche(ds)
    def up(ds: Directions): Directions = enHaut(ds)
    def down(ds: Directions): Directions = enBas(ds)

    def forward(ds: Directions): Directions = enAvant(ds)

    def toPosition(p: Position)(ds: Directions): Directions = versPos(p)(ds)

    def cornerUpLeft = coinHautGauche
    def cornerUpRight = coinHautDroite
    def cornerDownRight  = coinBasDroite
    def cornerDownLeft  = coinBasGauche

    def toAMonster(ds: Directions): Directions = versUnMonstre(ds)
    def farFromMonsters(ds: Directions): Directions = loinDesMonstres(ds)
    def toAPoint(ds: Directions): Directions = versUnPoint(ds)
    def farFromPoints(ds: Directions): Directions = loinDesPoints(ds)
    def toACherry(ds: Directions): Directions = versUneCerise(ds)
    def farFromCherries(ds: Directions): Directions = loinDesCerises(ds)
    def toPacMan(ds: Directions): Directions = versPacMan(ds)
    def farFromPacMan(ds: Directions): Directions = loinDePacMan(ds)

    def toPacManWithoutMonsterOnWay(ds: Directions): Directions = versPacManSansMonstreSurLeChemin(ds)

    def inSecurityFor(n: Int)(ds: Directions): Directions = enSecuritePendant(n)(ds)



    /**
     * Internal stuff
     */
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

    def directionsEnAvant: Directions = {
      val ahead = nextOpt(c.dir)
      val left  = nextOpt(c.dir.left)
      val right = nextOpt(c.dir.right)

      Directions((ahead :: left :: right :: Nil).collect{ case Some(pos) => pos }.toSet)
    }

    def minDistToVia(to: Set[Position], ds: Directions) = {
      val dists = withDistBetween(ds.dirs, to).sortWith((a, b) => a._2 < b._2)
      randomBestDir(dists)
    }

    def maxDistToVia(to: Set[Position], ds: Directions) = {
      randomBestDir(withDistBetween(ds.dirs, to).sortWith((a, b) => a._2 > b._2))
    }

    def maxWeightedDistToVia(to: Set[Position], ds: Directions, weightedDirs: Directions, weight: Int) = {
      randomBestDir(withDistBetween(ds.dirs, to).map(d => (d._1, if (((2*d._2) > weight) && (weightedDirs.dirs contains d._1)) d._2+weight else d._2)).sortWith((a, b) => a._2 > b._2))
    }

    def maxPathToVia(to: Set[Position], ds: Directions) = {
      randomBestDir(withPathBetween(ds.dirs, to).sortWith((a, b) => a._2 > b._2))
    }

    def randomBestDir(positions: Seq[(Direction, Int)]): Directions = {
      if (positions.size > 0) {
        val best = positions.filter(_._2 == positions.head._2)

        Directions(Set[Direction]() + best(randomInt(best.size))._1)
      } else {
        NoDirections
      }
    }

    def withPathBetween(directions: Set[Direction], to: Set[Position]): Seq[(Direction, Int)] = {
      directions.toSeq.map(dir =>
        (dir, model.maxPathBetween(c.pos, dir, to))
      )
    }

    def withDistBetween(directions: Set[Direction], to: Set[Position]): Seq[(Direction, Int)] = {
      directions.toSeq.map(dir =>
        (dir, model.minDistBetween(c.pos, c.pos.nextIn(dir), to, Set[Position]()))
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

  def next(model: Model, c: Figure) = {
    try {
      getMethod(model, c)()
    } catch {
      case e =>
        println("Exception in next(): "+ e)
        e.printStackTrace
        None 
    }
  }



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
    def telQue(cond: Filter) = cond(this)

    def vers(cond: Filter) = telQue(cond)

    def ouAlors(body: => Directions) = {
      if (!dirs.isEmpty) {
        this
      } else {
        body
      }
    }

    def sinon(body: => Directions) = ouAlors(body)

    def suchThat(cond: Filter) = telQue(cond)
    def to(cond: Filter) = suchThat(cond)
    def orElse(body: => Directions) = ouAlors(body)
    def otherwise(body: => Directions) = ouAlors(body)
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
      Some(dirs.dirs.toList(randomInt(dirs.dirs.size)))
    }
  }

}

abstract class DefaultPacManBehavior extends Behavior {
  import mvc._
  def getMethod(model: Model, p: Figure) = {
    new NextMethod(model, p) {
      def apply = reste /*{
        siChasseur {
          bouge telQue versUnMonstre
        } sinon {
          siMonstresLoin {
            bouge telQue versUnPoint
          } sinon {
            bouge telQue loinDesMonstres
          }
        }
      }
*/    }
  }
}

abstract class DefaultMonsterBehavior extends Behavior {
  import mvc._
  def getMethod(model: Model, p: Figure) = {
    new NextMethod(model, p) {
      def apply = /*bouge vers enAvant*/ {
        siChasseur {
          auHasard {
            bouge vers loinDePacMan
          } sinon {
            bouge vers enAvant
          }
        } sinon {
          alterner(25) {
            bouge vers enAvant telQue versPacMan
          } sinon {
            alterner(10) {
              bouge
            } sinon {
              bouge vers enAvant
            }
          }
        }
      }
    }
  }
}
