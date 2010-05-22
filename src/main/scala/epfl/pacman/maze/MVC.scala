package epfl.pacman
package maze

import epfl.pacman.interface.GUIs
import compiler.BehaviorCompiler

/*
GENERAL COMPOSITION STRUCTURE

trait Models extends Thingies with Positions with Directions { this: MVC =>
  class Model
}

trait Thingies { this: Models => }
trait Positions { this: Models => }
trait Directions { this: Models => }

trait Views { this: MVC =>
  class View
}

trait Controllers { this: MVC =>
  class Controller {
    var b = new Behavior { val mvc: Controllers.this.type = Controllers.this }
    def go { b.next(model) }
    def load(l: Load) { b = l.nb }
  }

  case class Load(nb: Behavior { val mvc: Controllers.this.type })
}

trait MVC extends Models with Views with Controllers {
  val model = new Model
  val view = new View
  val controller = new Controller
}

abstract class Behavior {
  val mvc: MVC
  def next(m: mvc.Model) = 1
}

abstract class BehaviorCompiler {
  val mvc: MVC
  def compile {
    val b = new Object
    mvc.controller.load(mvc.Load(b.asInstanceOf[Behavior { val mvc: BehaviorCompiler.this.mvc.type } ]))
  }
}

*/

class MVC extends Models with Views with GUIs with Controllers {
  var model = new Model()
  val view = new View()
  val gui = new PacmanApp()
  val controller = new Controller()

  val compiler = new BehaviorCompiler {
    val mvc: MVC.this.type = MVC.this
  }
}
