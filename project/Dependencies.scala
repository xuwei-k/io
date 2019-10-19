import sbt._
import Keys._

object Dependencies {
  val scala212 = "2.12.10"
  val scala213 = "2.13.1"

  val scalaCompiler = Def.setting { "org.scala-lang" % "scala-compiler" % scalaVersion.value }

  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.2"
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.8"
  val jna = "net.java.dev.jna" % "jna" % "4.5.2"
  val jnaPlatform = "net.java.dev.jna" % "jna-platform" % "4.5.2"
  val swovalFiles = "com.swoval" % "file-tree-views" % "2.1.3"
  def nightlyVersion: Option[String] = sys.props.get("sbt.build.version")
}
