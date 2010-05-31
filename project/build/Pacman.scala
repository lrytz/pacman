import sbt._
import proguard.{Configuration=>ProGuardConfiguration, ProGuard, ConfigurationParser}

class Pacman(info: ProjectInfo) extends ProguardProject(info) {
  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.0.RC2"


  // @TODO: doesn't work...

  val args =
"""-keep public class epfl.pacman.Main {
    public static void main(java.lang.String[]);
  }""" ::
//"-keep class epfl.pacman.**" ::
"-injars project/boot/scala-2.8.0.RC2/lib/scala-library.jar" ::
"-injars project/boot/scala-2.8.0.RC2/lib/scala-compiler.jar" ::
"-injars lib_managed/scala_2.8.0.RC2/compile/scala-swing-2.8.0.RC2.jar" ::
"-injars target/scala_2.8.0.RC2/pacman_2.8.0.RC2-1.0.jar" ::
"-dontwarn" :: "-dontoptimize" :: "-dontobfuscate" ::
"-outjars target/scala_2.8.0.RC2/pacman_2.8.0.RC2-1.0.min.jar" ::
//"-libraryjars "+System.getProperty("java.home")+"/lib/rt.jar" ::
Nil

  override def proguardTask = task {
    val config = new ProGuardConfiguration
    new ConfigurationParser(args.toArray[String], info.projectPath.asFile).parse(config)
    new ProGuard(config).execute
    None
  }
}
