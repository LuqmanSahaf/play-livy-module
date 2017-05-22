name := """sample"""

organization := "com.github.luqmansahaf"

version := "1.0-SNAPSHOT"

//lazy val module = RootProject(file("../play-livy"))

//lazy val root = (project in file(".")).enablePlugins(PlayScala).dependsOn(module)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq("com.github.luqmansahaf" %% "play-livy" % "1.0-SNAPSHOT")
