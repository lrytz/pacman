import sbt._
import proguard.{Configuration=>ProGuardConfiguration, ProGuard, ConfigurationParser}

class Pacman(info: ProjectInfo) extends DefaultProject(info) with ProguardProject {
  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.1"

  override val artifactID = "pacman"

  override def mainClass = Some("epfl.pacman.Main")


  // @TODO: for now, it just includes everything. also manually specifying jars is ugly.
  // http://stackoverflow.com/questions/2887655/making-stand-alone-jar-with-simple-build-tool

  override def proguardInJars = super.proguardInJars +++ scalaLibraryPath +++ Path.fromFile("project/boot/scala-2.8.1/lib/scala-compiler.jar") +++ Path.fromFile("lib_managed/scala_2.8.1/compile/scala-swing-2.8.1.jar")

  override def proguardOptions = List(
    "-keep class epfl.** { *; }",
    "-keep class scala.** { *; }",
    "-dontoptimize",
    "-dontobfuscate",
    proguardKeepLimitedSerializability,
    proguardKeepAllScala,
    "-keep class ch.epfl.** { *; }")
}

