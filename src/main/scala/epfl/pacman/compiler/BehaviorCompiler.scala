package epfl.pacman
package compiler

import tools.nsc.{Global, Settings}
import tools.nsc.io.VirtualDirectory
import tools.nsc.interpreter.AbstractFileClassLoader
import tools.nsc.reporters.AbstractReporter
import java.lang.String
import tools.nsc.util.{Position => FilePosition, BatchSourceFile}
import collection.mutable.{Set, HashSet}
import maze.MVC
import behaviour.Behavior

abstract class BehaviorCompiler {

  val mvc: MVC
  
  private val template =
"""package epfl.pacman
package behaviour

import maze.MVC

object Factory {
  // could use dependent method type
  def create(theMVC: MVC): Behavior = new Behavior {
    val mvc = theMVC
    import mvc._
    def getMethod(model: Model, p: Figure) = {
      new NextMethod(model, p) {
        def apply = {
          %s
        }
      }
    }
  }
}"""

  // number of lines before user's text
  private val errorOffset = 13

  private val settings = new Settings()

  settings.classpath.value = {
    def pathOf(cls: Class[_]) = {
      val l = cls.getProtectionDomain.getCodeSource.getLocation
      new java.io.File(l.toURI).getAbsolutePath
    }
    pathOf(this.getClass) +":"+ pathOf(classOf[ScalaObject]) +":"+ pathOf(classOf[Global])
  }

  private val outDir = new VirtualDirectory("(memory)", None)
  settings.outputDirs.setSingleOutput(outDir)

  private val reporter = new AbstractReporter {
    val settings = BehaviorCompiler.this.settings

    val errorPositions: Set[FilePosition] = new HashSet()

    override def reset {
      errorPositions.clear()
      super.reset
    }

    def display(pos: FilePosition, msg: String, severity: Severity) {
      println(msg)
      printSourceLine(pos)
      severity.count += 1
      if (severity == ERROR)
        errorPositions += pos
    }

   def printSourceLine(pos: FilePosition) {
     println(pos.lineContent.stripLineEnd)
     printColumnMarker(pos)
   }

   def printColumnMarker(pos: FilePosition) =
     if (pos.isDefined) { println(" " * (pos.column - 1) + "^") }


    def displayPrompt {
      fatal()
    }

    def fatal() {

    }
  }

  private val global = new Global(settings, reporter)

  def compile(body: String) {
    val t = new Thread() {
      override def run() {

        val source = template.format(body)
        val run = new global.Run
        val file = new BatchSourceFile("<behavior>", source)
        run.compileSources(List(file))

        if (reporter.hasErrors) {
          val errorLines = reporter.errorPositions.map(_.line - errorOffset)
          val text = errorLines.mkString("erroneous line(s): ", ", ", "")
          println(text)
          swing.Swing.onEDT {
            mvc.controller ! mvc.Resume
            mvc.gui.setErrors(errorLines)
            mvc.gui.unlock()
          }
        } else {
          val parent = this.getClass.getClassLoader
          val classLoader = new AbstractFileClassLoader(outDir, parent)

          val mvcClass = classOf[MVC]

          val behavior = classLoader.findClass("epfl.pacman.behaviour.Factory")
          val m = behavior.getMethod("create", mvcClass)
          val behaviorInst = m.invoke(null, mvc).asInstanceOf[Behavior { val mvc: BehaviorCompiler.this.mvc.type }]

          swing.Swing.onEDT {
            mvc.controller ! mvc.Load(behaviorInst)
          }
        }
        reporter.reset
      }
    }
    t.start()
  }

}
