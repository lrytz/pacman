package epfl.pacman.maze.controller

import actors.Actor
import epfl.pacman.Settings

class Ticker(controller: Controller) extends Actor {
  def act() {
    while(true) {
      controller ! Tick
      Thread.sleep(Settings.sleepTime)
    }
  }
}
