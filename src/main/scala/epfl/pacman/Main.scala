package scala.epfl.pacman

import model._
import gui.pacman.View

object Main {
    val rnd = new scala.util.Random(System.currentTimeMillis)

    def main(args: Array[String]): Unit = {
        var model = new Model()

        val v = new View(model)
    }

}
