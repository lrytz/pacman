package epfl.pacman
package compiler

import maze.MVC
import tools.nsc.{Global, Settings}
import tools.nsc.io.VirtualDirectory
import tools.nsc.interpreter.AbstractFileClassLoader
import tools.nsc.util.{ScalaClassLoader, BatchSourceFile}
import tools.util.PathResolver

class BehaviorCompiler(mvc: MVC) {
  private val template =
"""package epfl.pacman
package behaviour

import maze.MVC

class SubBehaviors(val mvc: MVC) {
  import mvc._
  class SubBehavior extends Behavior {
%s
  }
}"""

  private val settings = new Settings()

  settings.classpath.value = {
    def pathOf(cls: Class[_]) = {
      val l = cls.getProtectionDomain.getCodeSource.getLocation
      new java.io.File(l.toURI).getAbsolutePath
    }
    pathOf(this.getClass) +":"+ pathOf(classOf[ScalaObject]) +":"+ pathOf(classOf[Global])
  }

  val outDir = new VirtualDirectory("(memory)", None)
  settings.outputDirs.setSingleOutput(outDir)

  // output to VirtualDirectory

  private val global = new Global(settings)

  def compile(body: String, finish: () => Unit) {
    val t = new Thread() {
      override def run() {

        val source = template.format(body)
        val run = new global.Run
        val file = new BatchSourceFile("<behavior>", source)
        run.compileSources(List(file))

        val parent = this.getClass.getClassLoader
        val classLoader = new AbstractFileClassLoader(outDir, parent)

        val behaviors = classLoader.findClass("epfl.pacman.behaviour.SubBehaviors")
        val behaviorsConstr = behaviors.getConstructors.apply(0)
        val behaviorsInst = behaviorsConstr.newInstance(mvc).asInstanceOf[AnyRef]

        val behavior = classLoader.findClass("epfl.pacman.behaviour.SubBehaviors$SubBehavior")
        val behaviorConstr = behavior.getConstructors.apply(0)
        val behaviorInst = behaviorConstr.newInstance(behaviorsInst).asInstanceOf[mvc.Behavior]

        println("res: "+ behaviorInst)

        swing.Swing.onEDT {
          mvc.controller ! mvc.Load(behaviorInst)
          mvc.controller ! mvc.Resume
          finish()
        }
      }
    }
    t.start()
  }

}