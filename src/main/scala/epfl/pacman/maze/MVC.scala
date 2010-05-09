package epfl.pacman
package maze

import behaviour.Behaviors

class MVC extends Models with Views with Controllers with Thingies with Positions with Directions with Behaviors {
  var model = new Model()
  val view = new View()
  val controller = new Controller()
}
