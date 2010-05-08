package epfl.pacman

import maze.model._
import maze.view._

object Main {
    val rnd = new scala.util.Random(System.currentTimeMillis)

    def main(args: Array[String]): Unit = {
        var model = new Model()

        val v = new View(model)
    }

}
