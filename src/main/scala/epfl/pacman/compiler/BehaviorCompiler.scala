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

trait Behaviors { this: MVC =>

  class Behavior {
    def next(model: Model, character: Figure): (Position, Direction) = {
%s
    }
  }
}"""

  private val settings = new Settings()

  settings.classpath.value = {
    val l = this.getClass.getProtectionDomain.getCodeSource.getLocation
    new java.io.File(l.toURI).getAbsolutePath
  }

  val outDir = new VirtualDirectory("(memory)", None)
  settings.outputDirs.setSingleOutput(outDir)

  // output to VirtualDirectory

  private val global = new Global(settings)

  def compile(body: String) {
    val source = template.format(body)
    val run = new global.Run
    val file = new BatchSourceFile("<behavior>", source)
    run.compileSources(List(file))

    val parent = ScalaClassLoader.fromURLs(new PathResolver(settings).asURLs)
    val classLoader = new AbstractFileClassLoader(outDir, parent)

    val c = classLoader.findClass("epfl.pacman.Behaviors$Behavior")
    val i = c.newInstance().asInstanceOf[mvc.Behavior]
    mvc.controller ! mvc.Pause
    mvc.controller ! mvc.Load(i)
    mvc.controller ! mvc.Resume
  }

}