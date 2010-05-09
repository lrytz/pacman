package epfl.pacman
package interface

import swing._
import java.awt.{Color, Insets}
import maze.MVC

class PacmanApp(mvc: MVC) extends SimpleSwingApplication {

  import mvc._

  // not used right now, but this is what they should be...
  val width = 10 + Settings.docTextWidth + 10 + Settings.codeTextWidth + 10 + view.width + 10
  val height = view.height

  def top = new MainFrame {
    title = "Scala Pacman"
    background = Color.BLACK



    val game = new GridBagPanel {
      import GridBagPanel._
      val c = new Constraints

      val doc = new TextArea()
      c.fill = Fill.Both
      c.ipadx = Settings.docTextWidth
      c.gridheight = 3
      c.gridx = 0
      c.gridy = 0
      c.weighty = 1.0
      c.insets = new Insets(10, 10, 10, 5) // top, left, bottom, right
      layout(doc) = c

      val code = new TextArea()
      c.fill = Fill.Both
      c.ipadx = Settings.codeTextWidth
      c.gridheight = 1
      c.gridx = 1
      c.gridy = 0
      c.weighty = 1.0
      c.insets = new Insets(10, 5, 5, 5)
      layout(code) = c

      val runButton = new Button("Lancer!")
      c.fill = Fill.Horizontal
      c.gridx = 1
      c.gridy = 1
      c.weighty = 0.0
      c.insets = new Insets(5, 5, 0, 5)
      layout(runButton) = c

      val pauseButton = new Button("Arreter...")
      c.fill = Fill.Horizontal
      c.gridx = 1
      c.gridy = 2
      c.insets = new Insets(0, 5, 10, 5)
      layout(pauseButton) = c

      val maze = view
      c.fill = Fill.Both
      c.gridheight = 3
      c.gridx = 2
      c.gridy = 0
      c.weightx = 1.0
      c.weighty = 1.0
      c.insets = new Insets(10, 5, 10, 10)
      layout(maze) = c
    }

    contents = new ScrollPane {
      contents = game
    }

    maximize()    
  }


}
