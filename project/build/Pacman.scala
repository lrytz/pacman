import sbt._
import proguard.{Configuration=>ProGuardConfiguration, ProGuard, ConfigurationParser}

class Pacman(info: ProjectInfo) extends DefaultProject(info) with ProguardProject {
  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.0"

  override val artifactID = "pacman"

  override def mainClass = Some("epfl.pacman.Main")


  override def proguardOptions = List(
   "-keepclasseswithmembers public class * { public static void main(java.lang.String[]); }",
    "-dontoptimize",
    "-dontobfuscate",
    proguardKeepLimitedSerializability,
    proguardKeepAllScala,
    "-keep interface scala.ScalaObject"
  )

  override def proguardInJars = Path.fromFile("lib_managed/scala_2.8.0/compile/scala-swing-2.8.0.jar") +++ Path.fromFile(scalaLibraryJar) +++ super.proguardInJars
}
