name := """sample"""

organization := "luqman.sahaf"

version := "1.0-SNAPSHOT"

//lazy val module = RootProject(file("../play-livy"))

//lazy val root = (project in file(".")).enablePlugins(PlayScala).dependsOn(module)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "luqman.sahaf" %% "play-livy" % "1.0-SNAPSHOT"
)
