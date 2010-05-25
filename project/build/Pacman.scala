import sbt._

class Pacman(info: ProjectInfo) extends DefaultProject(info) {
  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.0.RC2"
}
