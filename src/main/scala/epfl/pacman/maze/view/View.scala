package epfl.pacman
package maze.view

import javax.swing.JComponent
import javax.imageio.ImageIO
import java.io.File
import java.awt.{Graphics, Graphics2D, Color }
import swing._
import maze.model._

class View(var model: Model) extends Component {

  import Settings._

  val width = vBlocks * blockSize
  val height = hBlocks * blockSize


  override lazy val peer = new JComponent() {
    override def paintComponent(graph: Graphics) {
      val g = graph.asInstanceOf[Graphics2D]

      g.setColor(Color.BLACK)

      for (w <- model.walls) {
        drawWall(w, g)
      }

      for (m <- model.monsters) {
        drawMonster(m, g)
      }

      drawPacman(model.pacman, g)
    }
  }

  def toAbs(x: Int, o: Int = 0) = x * blockSize + o

  def drawPacman(p: Figure, g: Graphics2D) = {
    g.setColor(Color.YELLOW)

    val radius = blockSize/2 - 5
    val centerX = toAbs(p.pos.x, p.pos.xo) + blockSize/2
    val centerY = toAbs(p.pos.y, p.pos.yo) + blockSize/2

    val angle = 30

    val startAngle = p.dir match {
      case Left  => 180 + angle
      case Right => 0 + angle
      case Down  => 270 + angle
      case Up    => 90 + angle
    }

    g.fillArc(centerX - radius, centerY - radius, 2*radius, 2*radius, startAngle, 360 - 2*angle)
  }

  val image = ImageIO.read(new File("src/main/resources/badguy1.png"))

  def drawMonster(m: Figure, g: Graphics2D) = {
    g.drawImage(image, toAbs(m.pos.x, m.pos.xo), toAbs(m.pos.y, m.pos.yo) + 6, null)
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
      g.drawPolyline(Array(x, x+blockSize), Array(y, y), 2)
      tborder = 5
    }

    if (!model.isWallAt(w.pos.onBottom)) {
      g.drawPolyline(Array(x, x+blockSize), Array(y+blockSize, y+blockSize), 2)
      bborder = 5
    }

    if (!model.isWallAt(w.pos.onLeft)) {
      g.drawPolyline(Array(x, x), Array(y, y+blockSize), 2)
      lborder = 3
    }

    if (!model.isWallAt(w.pos.onRight)) {
      g.drawPolyline(Array(x+blockSize, x+blockSize), Array(y, y+blockSize), 2)
      rborder = 3
    }

    g.setColor(Color.BLUE)
    for (i <- tborder to blockSize-bborder by 5) {
      g.drawPolyline(Array(x+lborder, x+blockSize-rborder), Array(y + i, y + i), 2)
    }
  }

}
