package epfl.pacman

import javax.swing.JApplet

import scala.swing._
import maze.MVC
import java.awt.Color

class MainApplet extends JApplet {

  this.setBackground(Color.BLACK)

  val mvc = new MVC

  this.add(mvc.gui.peer)

  mvc.controller.start()
  Thread.sleep(1000)
  mvc.ticker.start()

}