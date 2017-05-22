name := "play-livy"

organization := "com.github.luqmansahaf"

version := "1.0-SNAPSHOT"

lazy val `playlivy` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies += "com.cloudera.livy" % "livy-client-http" % "0.3.0"

libraryDependencies += "com.cloudera.livy" % "livy-api" % "0.3.0"

libraryDependencies += "com.cloudera.livy" %% "livy-scala-api" % "0.3.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.0.2"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
