package epfl.pacman

import scala.swing._
import maze.MVC
import java.awt.Color

object Main extends SimpleSwingApplication {

  val mvc = new MVC

  def top = new MainFrame {
      title = mvc.text("titleText")
      background = Color.BLACK
      contents = mvc.gui
      maximize()
  }

  mvc.controller.start()
  Thread.sleep(1000)
  mvc.ticker.start()

}
