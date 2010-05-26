package epfl.pacman

import maze.MVC

object Main {
  val rnd = new scala.util.Random(System.currentTimeMillis)

  def main(args: Array[String]): Unit = {
    val mvc = new MVC

    mvc.gui.main(args)

    mvc.controller.start()
    Thread.sleep(1000)
    mvc.ticker.start()
  }
}
