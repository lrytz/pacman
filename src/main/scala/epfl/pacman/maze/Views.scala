package epfl.pacman
package maze

import javax.swing.JComponent
import javax.imageio.ImageIO
import java.io.File
import java.awt.{Graphics, Graphics2D, Color}
import swing._

trait Views { this: MVC =>

  val view: View

  class View extends Component {
    import Settings._

    val width = hBlocks * blockSize + 1 // one more for the border
    val height = vBlocks * blockSize + 1

    preferredSize = new Dimension(width, height)

    override def paintComponent(g: Graphics2D) {

      g.setColor(Color.BLACK)

      for (w <- model.walls) {
        drawWall(w, g)
      }

      for (m <- model.monsters) {
        drawMonster(m, g)
      }

      // this makes look pacman much better (do it here, not for walls, for efficiency)
      import java.awt.RenderingHints.{KEY_ANTIALIASING, VALUE_ANTIALIAS_ON}
      g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)

      drawPacman(model.pacman, g)
    }

    @inline final def toAbs(x: Int, o: Int = 0) = x * blockSize + o

    def drawPacman(p: Figure, g: Graphics2D) = {
      g.setColor(Color.YELLOW)

      val radius = blockSize/2 - 5
      val centerX = toAbs(p.pos.x, p.pos.xo) + blockSize/2
      val centerY = toAbs(p.pos.y, p.pos.yo) + blockSize/2

      val angle = 30

      val startAngle = (p.dir match {
        case Up    => 90
        case Left  => 180
        case Down  => 270
        case Right => 0
      }) + angle

      g.fillArc(centerX - radius, centerY - radius, 2*radius, 2*radius, startAngle, 360 - 2*angle)
    }

    val image = ImageIO.read(new File("src/main/resources/badguy1.png"))

    def drawMonster(m: Figure, g: Graphics2D) = {
      val xOffset = 3
      val yOffset = 6

      g.drawImage(image, toAbs(m.pos.x, m.pos.xo) + xOffset, toAbs(m.pos.y, m.pos.yo) + yOffset, null)
    }

    /**
     * performance
     *
     * using 10 ms as sleep time between ticks
     *
     * "pause" uses 12 % cpu on my machine
     * "run" uses 80 % cpu
     * without drawing the walls, the game uses 26 % cpu
     *
     * 77 % is drawing
     *   73 % walls, all time is spent in drawPolyLine
     *   3  % monsters
     *   1  % pacman
     *
     * 19 % is paintComponent
     *
     * without walls, sleep time of 3 ms works fine (45 % cpu).
     * with walls, this takes 75 % and the animation is often not fluent.
     *
     * so we need another layer for the walls which does not get re-painted when thingies move.
     *
     */

    def drawWall(w: Wall, g: Graphics2D) = {
      // Based on the walls around, draw differently
      g.setColor(Color.CYAN)
      val x = toAbs(w.pos.x)
      val y = toAbs(w.pos.y)

      var lborder = 0
      var rborder = 0
      var tborder = 0
      var bborder = 0

//      if (!model.isWallAt(w.pos.onTop)) {
//        g.drawLine(x, y, x+blockSize, y)
//        tborder = 5
//      }
//
//      if (!model.isWallAt(w.pos.onBottom)) {
//        g.drawLine(x, y+blockSize, x+blockSize, y+blockSize)
//        bborder = 5
//      }
//
//      if (!model.isWallAt(w.pos.onLeft)) {
//        g.drawLine(x, y, x, y+blockSize)
//        lborder = 3
//      }
//
//      if (!model.isWallAt(w.pos.onRight)) {
//        g.drawLine(x+blockSize, y, x+blockSize, y+blockSize)
//        rborder = 3
//      }

      g.setColor(Color.BLUE)
      for (i <- tborder to blockSize-bborder by 5) {
        g.drawLine(x+lborder, y + i, x+blockSize-rborder, y + i)
      }
    }

  }
}