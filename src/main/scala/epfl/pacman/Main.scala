package epfl.pacman

import maze.MVC

object Main {
  val rnd = new scala.util.Random(System.currentTimeMillis)

  def main(args: Array[String]): Unit = {
    val mvc = new MVC

    new interface.PacmanApp(mvc).main(args)

    mvc.controller.start()
    Thread.sleep(2000)
    mvc.ticker.start()
  }

}
