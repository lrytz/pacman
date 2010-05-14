package epfl.pacman
package maze

import javax.imageio.ImageIO
import java.io.File
import java.awt.{Graphics2D, Color}
import java.awt.image.BufferedImage
import swing._
import Swing._

trait Views { this: MVC =>

  val view: View

  class View extends Component {
    import Settings._

    val width = hBlocks * blockSize + 1 // one more for the border
    val height = vBlocks * blockSize + 1

    preferredSize = (width, height)

    // render the walls into an image. much faster than re-painting at every tick.
    val maze = {
      val img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
      val g = img.getGraphics().asInstanceOf[Graphics2D]
      for (w <- model.walls) {
        drawWall(w, g)
      }
      img
    }


    override def paintComponent(g: Graphics2D) {

      g.setColor(Color.BLACK)

      g.drawImage(maze, 0, 0, null)

      for (m <- model.monsters) {
        drawMonster(m, g)
      }

      // this makes look pacman much better (for efficiency only here, not for the walls / monsters)
      import java.awt.RenderingHints.{KEY_ANTIALIASING, VALUE_ANTIALIAS_ON}
      g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)

      drawPacman(model.pacman, g)
    }

    @inline final def toAbs(x: Int, o: Int = 0) = x * blockSize + o

    def drawPacman(p: PacMan, g: Graphics2D) = {
      g.setColor(Color.YELLOW)

      val radius = blockSize/2 - 5
      val centerX = toAbs(p.pos.x, p.pos.xo) + blockSize/2
      val centerY = toAbs(p.pos.y, p.pos.yo) + blockSize/2

      val angle = p.angle.value

      val startAngle = (p.dir match {
        case Up    => 90
        case Left  => 180
        case Down  => 270
        case Right => 0
      }) + angle

      g.fillArc(centerX - radius, centerY - radius, 2*radius, 2*radius, startAngle, 360 - 2*angle)
    }

    val images = ("src/main/resources/badguy0.png" ::
                  "src/main/resources/badguy1.png" ::
                  "src/main/resources/badguy2.png" ::
                  "src/main/resources/badguy3.png" :: Nil).map(path => ImageIO.read(new File(path)))


    def drawMonster(m: Monster, g: Graphics2D) = {
      val xOffset = 3
      val yOffset = 6

      val img = if (m.laser.status) { images((m.laser.animOffset/2 % 3) + 1) } else { images(0) }

      g.drawImage(img, toAbs(m.pos.x, m.pos.xo) + xOffset, toAbs(m.pos.y, m.pos.yo) + yOffset, null)
    }

    def drawWall(w: Wall, g: Graphics2D) = {
      // Based on the walls around, draw differently
      g.setColor(Color.CYAN)
      val x = toAbs(w.pos.x)
      val y = toAbs(w.pos.y)

      var lborder = 0
      var rborder = 0
      var tborder = 0
      var bborder = 0

      if (!model.isWallAt(w.pos.onTop)) {
        g.drawLine(x, y, x+blockSize, y)
        tborder = 5
      }

      if (!model.isWallAt(w.pos.onBottom)) {
        g.drawLine(x, y+blockSize, x+blockSize, y+blockSize)
        bborder = 5
      }

      if (!model.isWallAt(w.pos.onLeft)) {
        g.drawLine(x, y, x, y+blockSize)
        lborder = 3
      }

      if (!model.isWallAt(w.pos.onRight)) {
        g.drawLine(x+blockSize, y, x+blockSize, y+blockSize)
        rborder = 3
      }

      g.setColor(Color.BLUE)
      for (i <- tborder to blockSize-bborder by 5) {
        g.drawLine(x+lborder, y + i, x+blockSize-rborder, y + i)
      }
    }

  }
}
