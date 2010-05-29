package epfl.pacman.editor

import scala.collection._

import swing.Component
import swing.Swing._
import javax.swing.JTextPane
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.text._
import util.matching.Regex
import java.awt.event.{KeyEvent, KeyListener}
import java.awt.Color

class ScalaPane extends Component { thisPane =>

  def text: String =
    document.getText(0, document.getLength)

  def text_=(c: String): Unit = {
    document.replace(0, document.getLength, c, null)
    notifyUpdate()
  }

  protected var generation: Int = 0

  protected object linesCache {
    var generation: Int = thisPane.generation
    var lines: List[ScalaLine] = null
  }

  def lines: List[ScalaLine] = {
    if (true || (linesCache.generation != thisPane.generation) || (linesCache.lines == null)) {
      val lineEnding = new Regex("""\r\n|\r|\n|\u0085|\u2028|\u2029""")
      val text = lineEnding.replaceAllIn(thisPane.text, "\n")
      def lines0(idx: Int, startChar: Int): List[ScalaLine] = {
        val endChar = text.indexOf('\n', startChar)
        if (endChar < 0)
          new ScalaLine(idx, startChar, text.substring(startChar)) :: Nil
        else
          new ScalaLine(idx, startChar, text.substring(startChar, endChar)) :: lines0(idx + 1, endChar + 1)
      }
      linesCache.lines = lines0(idx = 0, startChar = 0)
    }
    linesCache.lines
  }

  class ScalaLine(idx0: Int, pos0: Int, text0: String) { thisLine =>

    protected var highlighted = false

    protected val generation = thisPane.generation

    def isValid: Boolean =
      thisLine.generation == thisPane.generation

    def range: (Int, Int) = (pos0, pos0 + text0.length)

    protected def checkGen[A](body: => Unit): Unit =
      if (thisLine.isValid) body else throw new IllegalStateException("Line " + idx0 + " is outdated generation")

    def text: String = text0

    def text_=(c: String): Unit = checkGen {
      document.replace(pos0, text0.length, c, null)
    }

    def highlight: Unit = checkGen {
      revealKeywords(style.highlight, style.highkey)
    }

    def removeHighlight: Unit = checkGen {
      revealKeywords(style.normal, style.keyword)
    }

    def revealKeywords(textStyle: AttributeSet, keywordStyle: AttributeSet): Unit = checkGen {
      val separators = new Regex(""" |\.|\(|\)|,|\t""") // must be single characters
      val text = separators.replaceAllIn(text0, " ")
      def highlight0(startChar: Int, word: String): Unit =
        if (keywords exists { k => (k compareTo word) == 0 }) {
          document.setCharacterAttributes(pos0 + startChar, word.length, keywordStyle, true)
        }
      def reveal0(startChar: Int): Unit = {
        val endChar = text.indexOf(' ', startChar)
        if (endChar < 0)
          highlight0(startChar, text.substring(startChar))
        else {
          highlight0(startChar, text.substring(startChar, endChar))
          reveal0(endChar + 1)
        }
      }
      document.setCharacterAttributes(pos0, text0.length, textStyle, true)
      reveal0(startChar = 0)
    }

  }

  val keywords: mutable.Set[String] =
    mutable.Set.empty[String]

  def notifyUpdate(): Unit =
    notifyUpdate(0, document.getLength)

  protected def notifyUpdate(editFrom: Int, editTo: Int): Unit = {
    thisPane.generation += 1
    if (peer.isShowing) { // component deadlocks if document edited during initialisation
      val touchedLines =
        lines filter { l =>
          val (lineFrom, lineTo) = l.range
          !((editTo < lineFrom) || (editFrom > lineTo))
        }
      //println("touchedLines = " + (touchedLines map { _.text }).mkString("\"", "\", \"", "\""))
      touchedLines foreach { _.removeHighlight }
    }
  }

  protected lazy val document = new DefaultStyledDocument {
    setParagraphAttributes(0, getLength, style.normal, true)
    setCharacterAttributes(0, getLength, style.normal, true)
    addDocumentListener(new DocumentListener {
      def changedUpdate(e: DocumentEvent) = {}
      def insertUpdate(e: DocumentEvent) = { onEDT(notifyUpdate(e.getOffset, e.getOffset + e.getLength)) }
      def removeUpdate(e: DocumentEvent) = { onEDT(notifyUpdate(e.getOffset, e.getOffset + e.getLength)) }
    })
  }

  override lazy val peer = new JTextPane(document) with SuperMixin

  protected object style {

    val defaultFont: String = "Menlo"

    val normal: AttributeSet = {
      val s = new SimpleAttributeSet
      StyleConstants.setForeground(s, Color.WHITE)
      StyleConstants.setFontFamily(s, defaultFont)
      StyleConstants.setFontSize(s, 14)
      s
    }

    val highlight: AttributeSet = {
      val s = new SimpleAttributeSet
      StyleConstants.setForeground(s, Color.RED)
      StyleConstants.setFontFamily(s, defaultFont)
      s
    }

    val keyword: AttributeSet = {
      val s = new SimpleAttributeSet
      StyleConstants.setBold(s, true)
      StyleConstants.setForeground(s, Color.GREEN)
      StyleConstants.setFontFamily(s, defaultFont)
      s
    }

    val highkey: AttributeSet = {
      val s = new SimpleAttributeSet
      StyleConstants.setBold(s, true)
      StyleConstants.setForeground(s, Color.RED)
      StyleConstants.setFontFamily(s, defaultFont)
      s
    }

  }
  
}
