package epfl.pacman
package behaviour

import maze.MVC

trait Behaviors { this: MVC =>

  class Behavior {
    def next(model: Model, character: Figure): (Position, Direction) = {
      def next(dir: Direction) = {
        val pos = character.pos.nextIn(dir)
        if (model.isWallAt(pos)) None
        else Some(pos, dir)

      }
      next(character.dir).getOrElse {
        val dirs = Array(character.dir.left, character.dir.right)
        java.util.Collections.shuffle(java.util.Arrays.asList(dirs: _*))
        next(dirs(0)).getOrElse {
          next(dirs(1)).getOrElse {
            next(character.dir.opposite).get
          }
        }
      }
    }
  }
  
}