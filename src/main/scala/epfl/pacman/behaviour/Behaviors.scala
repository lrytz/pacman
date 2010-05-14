package epfl.pacman
package behaviour

import maze.MVC

trait Behaviors { this: MVC =>

  def defaultBehavior =
"""override def next(model: Model, character: Figure): (Position, Direction) = {
  def nextOpt(dir: Direction) = {
    val pos = character.pos.nextIn(dir)
    if (model.isWallAt(pos)) None
    else Some(pos, dir)
  }

  val ahead = nextOpt(character.dir)
  val left = nextOpt(character.dir.left)
  val right = nextOpt(character.dir.right)
  val back = nextOpt(character.dir.opposite)

  val dirs = Array(ahead, left, right).filter(_.isDefined)
  if (dirs.isEmpty) back.get
  else {
    java.util.Collections.shuffle(java.util.Arrays.asList(dirs: _*))
    dirs(0).get
  }
}"""

  class Behavior {
    def next(model: Model, character: Figure): (Position, Direction) = {
      def nextIn(dir: Direction) = {
        val pos = character.pos.nextIn(dir)
        if (model.isWallAt(pos)) None
        else Some(pos, dir)

      }
      nextIn(character.dir).getOrElse {
        val dirs = Array(character.dir.left, character.dir.right)
        java.util.Collections.shuffle(java.util.Arrays.asList(dirs: _*))
        nextIn(dirs(0)).getOrElse {
          nextIn(dirs(1)).getOrElse {
            nextIn(character.dir.opposite).get
          }
        }
      }
    }
  }
  
}