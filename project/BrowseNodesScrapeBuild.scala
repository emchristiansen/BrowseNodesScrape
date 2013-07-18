import sbt._
import Keys._

import sbtassembly.Plugin._
import AssemblyKeys._

import com.typesafe.sbt.SbtStartScript

object BrowseNodesScrapeBuild extends Build {
  def extraResolvers = Seq(
    resolvers ++= Seq(
      "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
      "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      "typesafe-releases" at "http://repo.typesafe.com/typesafe/repo",
      "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository"
    )
  )

  val scalaVersionString = "2.10.2"

  def extraLibraryDependencies = Seq(
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.7.2",
      "net.databinder.dispatch" %% "dispatch-core" % "0.10.1",
      "org.slf4j" % "slf4j-simple" % "1.7.5",
      "org.scala-lang" %% "scala-pickling" % "0.8.0-SNAPSHOT",
      "org.apache.commons" % "commons-io" % "1.3.2"
    )
  )

  def updateOnDependencyChange = Seq(
    watchSources <++= (managedClasspath in Test) map { cp => cp.files })

  def scalaSettings = Seq(
    scalaVersion := scalaVersionString,
    scalacOptions ++= Seq(
      "-optimize",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:implicitConversions",
      // "-language:reflectiveCalls",
      "-language:postfixOps"
    )
  )

  def moreSettings =
    Project.defaultSettings ++
    extraResolvers ++
    extraLibraryDependencies ++
    scalaSettings ++
    assemblySettings ++
    SbtStartScript.startScriptForJarSettings ++
    updateOnDependencyChange

  val projectName = "BrowseNodesScrape"
  lazy val root = {
    val settings = moreSettings ++ Seq(name := projectName, fork := true)
    Project(id = projectName, base = file("."), settings = settings)
  }
}
