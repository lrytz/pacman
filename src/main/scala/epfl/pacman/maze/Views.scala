package epfl.pacman
package maze

import javax.imageio.ImageIO
import java.io.File
import java.awt.image.BufferedImage
import swing._
import Swing._
import java.awt.{Font, Graphics2D, Color}

trait Views { this: MVC =>

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

      // this makes look the points and pacman much better
      import java.awt.RenderingHints.{KEY_ANTIALIASING, VALUE_ANTIALIAS_ON}
      g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)

      if (!model.simpleMode)
        for (p <- model.points) {
          drawPoint(p, g)
        }

      for (m <- model.monsters) {
        drawMonster(m, g)
      }

      drawPacman(model.pacman, g)


      if (model.state != Running) {
        val cv = model.state match {
          case GameOver => 0x44ff0000
          case GameWon => 0x4400ff00
          case _ => 0x44ffffff
        }
        g.setColor(new Color(cv, true))
        g.fillRect(0, 0, width, height)

        val msg = model.state match {
          case Paused => "Jeu en pause..."
          case Loading(_) => "Code en charge..."
          case GameOver => "Game over..."
          case GameWon if model.simpleMode => "Jeu gagné, PacMan sauvé!"
          case GameWon => "Jeu gangné!"
          case LifeLost(_) => "Vie perdu!"
          case CompileError(_) => "Code incorrect!"
        }

        val font = new Font("Dialog", Font.BOLD, 24)
        g.setFont(font)
        val metrics = g.getFontMetrics(font)
        val (w, h) = (metrics.stringWidth(msg), metrics.getHeight)

        g.setColor(new Color(0xccffffff, true))
        val (padX, padY) = (20, 10)
        val (x, y) = ((width - w - padX) / 2 , (height - h - padY) / 2)
        g.fillRoundRect(x, y, w + padX, h + padY, 20, 20)

        g.setColor(Color.BLACK)
        val yOff = (metrics.getAscent() - metrics.getDescent()) / 2
        g.drawString(msg, (width - w) / 2, height / 2 + yOff)
      }
    }

    @inline final def toAbs(x: Int, o: Int = 0) = x * blockSize + o

    def drawPacman(p: PacMan, g: Graphics2D) = {
      if (!p.hunter) {
        g.setColor(Color.YELLOW)
      } else {
        if (((p.angle.value / 4) & 1) == 0) {
            g.setColor(Color.GREEN)
        } else {
            g.setColor(Color.RED)
        }
      }

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
    
    val cherryImg = ImageIO.read(new File("src/main/resources/cherry.png"))

    def drawPoint(p: Thingy, g: Graphics2D) = {
      g.setColor(Color.GRAY)


      p match {
        case _: NormalPoint =>
          val radius = 4
          val centerX = toAbs(p.pos.x, 0) + blockSize/2
          val centerY = toAbs(p.pos.y, 0) + blockSize/2

          g.fillArc(centerX - radius, centerY - radius, 2*radius, 2*radius, 0, 360)
        case _: SuperPoint =>
          val xOffset = 7
          val yOffset = 6

          g.drawImage(cherryImg, toAbs(p.pos.x, 0) + xOffset, toAbs(p.pos.y, 0) + yOffset, null)
      }
    }

    val imagesInfo = ("src/main/resources/badguy1-0.png" ::
                      "src/main/resources/badguy1-1.png" ::
                      "src/main/resources/badguy1-2.png" ::
                      "src/main/resources/badguy1-3.png" :: Nil).map(path => ImageIO.read(new File(path)))

    val imagesCerbero = ("src/main/resources/badguy2-0.png" ::
                         "src/main/resources/badguy2-1.png" ::
                         "src/main/resources/badguy2-2.png" ::
                         "src/main/resources/badguy2-3.png" :: Nil).map(path => ImageIO.read(new File(path)))

    def drawMonster(m: Monster, g: Graphics2D) = {
      val xOffset = 3
      val yOffset = 6

      val images = m.typ match {
        case Info =>
          imagesInfo
        case Cerbero =>
          imagesCerbero
      }

      val img = if (m.anim.status) { images((m.anim.animOffset/2 % (images.size-1)) + 1) } else { images(0) }

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

      if (w.pos.x == 0 || !model.isWallAt(w.pos.onLeft)) {
        g.drawLine(x, y, x, y+blockSize)
        lborder = 3
      }

      if (w.pos.x == Settings.hBlocks-1 || !model.isWallAt(w.pos.onRight)) {
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
