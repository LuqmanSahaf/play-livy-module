name := "play-livy"

organization := "com.github.luqmansahaf"

version := "1.0"

licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"))

homepage := Some(url("https://github.com/LuqmanSahaf/Play-Livy-Module"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/LuqmanSahaf/Play-Livy-Module"),
    "scm:git@github.com:LuqmanSahaf/Play-Livy-Module.git"
  )
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

pomIncludeRepository := { _ => false }

publishArtifact in Test := false

pomExtra := (
    <developers>
      <developer>
        <id>luqmansahaf</id>
        <name>Luqman Sahaf</name>
        <url>http://luqmansahaf.github.io</url>
      </developer>
    </developers>)

lazy val `playlivy` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies += "com.cloudera.livy" % "livy-client-http" % "0.3.0"

libraryDependencies += "com.cloudera.livy" % "livy-api" % "0.3.0"

libraryDependencies += "com.cloudera.livy" %% "livy-scala-api" % "0.3.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.0.2"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
