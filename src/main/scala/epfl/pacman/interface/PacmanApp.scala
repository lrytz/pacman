package epfl.pacman
package interface

import swing._
import event.ButtonClicked
import java.awt.{Color, Insets}
import maze.MVC
import editor.ScalaPane
import Swing._

class PacmanApp(mvc: MVC) extends SimpleSwingApplication {

  import mvc._

  // not used right now, but this is what they should be...
//  val width = 10 + Settings.docTextWidth + 10 + Settings.codeTextWidth + 10 + view.width + 10
//  val height = view.height

  def top = new MainFrame {
    title = "Scala Pacman"
    background = Color.BLACK

    val game = new GridBagPanel {
      import GridBagPanel._
      val c = new Constraints
      
      maximumSize = (Settings.docTextWidth + 10 + Settings.codeTextWidth + 10 + view.width+20, view.height + 20)
      preferredSize = (Settings.docTextWidth + 10 + Settings.codeTextWidth + 10 + view.width+20, view.height + 20)

      val doc = new ScalaPane()
      doc.preferredSize = new Dimension(Settings.docTextWidth, view.height)
      c.fill = Fill.None
//      c.ipadx = Settings.docTextWidth
      c.gridx = 0
      c.gridy = 0
      c.weighty = 0.0
      c.insets = new Insets(10, 10, 10, 5) // top, left, bottom, right
      layout(doc) = c

      val code = new GridBagPanel {
        val c = new Constraints

        val pane = new ScalaPane()
        pane.keywords ++= List("pac", "man")
        pane.preferredSize = new Dimension(Settings.codeTextWidth, 10)
        c.fill = Fill.Both
  //      c.ipadx = Settings.codeTextWidth
        c.gridx = 0
        c.gridy = 0
        c.weighty = 1.0
        c.insets = new Insets(10, 5, 5, 5)
        layout(pane) = c

        val runButton = new Button("Lancer!")
        c.fill = Fill.Horizontal
        c.gridx = 0
        c.gridy = 1
        c.weighty = 0.0
        c.insets = new Insets(5, 5, 0, 5)
        layout(runButton) = c

        val pauseButton = new Button("Arreter...")
        c.fill = Fill.Horizontal
        c.gridx = 0
        c.gridy = 2
        c.insets = new Insets(0, 5, 10, 5)
        layout(pauseButton) = c

        listenTo(pauseButton)
        reactions += {
          case ButtonClicked(`pauseButton`) =>
            if (mvc.model.paused) {
              pauseButton.text = "Arreter..."
              mvc.controller ! mvc.Resume
            } else {
              pauseButton.text = "Continuer..."
              mvc.controller ! mvc.Pause
            }
        }

      }

      c.fill = Fill.Both
      c.gridx = 1
      c.gridy = 0
      c.weightx = 0.0
      c.weighty = 0.0
      layout(code) = c

      c.fill = Fill.Both
      c.gridx = 2
      c.gridy = 0
      c.weightx = 0.0
      c.weighty = 0.0
      c.insets = new Insets(10, 5, 10, 10)
      layout(view) = c
    }

    contents = game

            /*new GridBagPanel {
      import GridBagPanel._
      val c = new Constraints

      c.gridx = 0
      c.gridy = 0
      c.weightx = 1.0

    }
    */

    maximize()
  }


}
