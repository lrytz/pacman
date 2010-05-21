package epfl.pacman
package interface

import swing._
import event.ButtonClicked
import java.awt.{Color, Insets}
import maze.MVC
import editor.ScalaPane
import Swing._
import compiler.BehaviorCompiler
import behaviour.Behavior

trait GUIs { this: MVC =>

  class PacmanApp extends SimpleSwingApplication {

    // not used right now, but this is what they should be...
    //  val width = 10 + Settings.docTextWidth + 10 + Settings.codeTextWidth + 10 + view.width + 10
    //  val height = 10 + view.height + 10

    def top = new MainFrame {
      title = "Scala Pacman"
      background = Color.BLACK

      val doc = new ScalaPane()
      doc.preferredSize = (Settings.docTextWidth, view.height)

      val code = new ScalaPane()
      code.text = Behavior.defaultBehavior
      code.keywords ++= Settings.keywords
      code.preferredSize = (Settings.codeTextWidth, 0)

      val runButton = new Button("Lancer!")

      val pauseButton = new Button("Arreter...")


      contents = new GridBagPanel {
        import GridBagPanel._
        val c = new Constraints

        c.fill = Fill.None
        c.gridx = 0
        c.gridy = 0
        c.insets = new Insets(10, 10, 10, 5) // top, left, bottom, right
        layout(doc) = c

        val middle = new GridBagPanel {
          val c = new Constraints

          c.fill = Fill.Vertical
          c.gridx = 0
          c.gridy = 0
          c.weighty = 1.0
          c.insets = new Insets(0, 0, 5, 0)
          layout(code) = c

          c.fill = Fill.Horizontal
          c.gridx = 0
          c.gridy = 1
          c.weighty = 0.0
          c.insets = new Insets(5, 0, 0, 0)
          layout(runButton) = c

          c.gridx = 0
          c.gridy = 2
          c.insets = new Insets(0, 0, 0, 0)
          layout(pauseButton) = c
        }

        c.fill = Fill.Vertical
        c.gridx = 1
        c.gridy = 0
        c.insets = new Insets(10, 5, 10, 5)
        layout(middle) = c

        c.fill = Fill.None
        c.gridx = 2
        c.gridy = 0
        c.insets = new Insets(10, 5, 10, 10)
        layout(view) = c
      }


      listenTo(runButton, pauseButton)
      reactions += {
        case ButtonClicked(`runButton`) =>
          controller ! Pause
          runButton.text = "Code en charge..."
          runButton.enabled = false
          pauseButton.enabled = false
          val comp = new BehaviorCompiler {
            val mvc: GUIs.this.type = GUIs.this
          }
          comp.compile(code.text, () => {
            runButton.text = "Lancer!"
            runButton.enabled = true
            pauseButton.enabled = true
          })

        case ButtonClicked(`pauseButton`) =>
          if (model.paused) {
            pauseButton.text = "Arreter..."
            controller ! Resume
          } else {
            pauseButton.text = "Continuer..."
            controller ! Pause
          }
      }


      maximize()
    }
  }
}


