package epfl.pacman

import maze.MVC

object Main {
  val rnd = new scala.util.Random(System.currentTimeMillis)

  def main(args: Array[String]): Unit = {
    val mvc = new MVC

    new interface.PacmanApp(mvc).main(args)
    
    Thread.sleep(3000)
    mvc.controller.start()
    mvc.ticker.start()
  }

}
