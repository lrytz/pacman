package scala.epfl.pacman

import model._
import gui.pacman.View

object Main {
    val rnd = new scala.util.Random(System.currentTimeMillis)

    def main(args: Array[String]): Unit = {
        var model = new Model()

        var pacman = model.pacman

        val v = new View(model)

        println("asd");
        Thread.sleep(1000);
        pacman = pacman.move(pacman.pos.onLeft)
        v.repaint(model.copy(pacman = pacman))
        Thread.sleep(1000);
        pacman = pacman.move(pacman.pos.onLeft)
        v.repaint(model.copy(pacman = pacman))
        Thread.sleep(1000);
        pacman = pacman.move(pacman.pos.onLeft)
        v.repaint(model.copy(pacman = pacman))
        println("asd");
    }

}
