package epfl.pacman.editor

import scala.swing._
import event.ButtonClicked

object Test extends SimpleSwingApplication {

  def top = new MainFrame {
    title = "Editor Test"
    contents = new GridBagPanel {
      import GridBagPanel._
      val pane = new ScalaPane {
        preferredSize = new Dimension(300, 300)
        keywords ++= List("je", "tu", "le", "la")
      }
      layout(pane) = new Constraints {
        gridx = 0
        gridy = 0
        insets = new Insets(10, 10, 10, 10)
      }
      val button = new Button("Test")
      layout(button) = new Constraints {
        gridx = 0
        gridy = 1
        insets = new Insets(10, 10, 10, 10)
      }
      listenTo(button)
      reactions += {
        case ButtonClicked(`button`) =>
          val line = pane.lines.length / 2
          println("highlighting line " + line)
          pane.lines(line).highlight
      }
    }
  }

}