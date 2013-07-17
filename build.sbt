
name:="fixture-state-management"

organization:="ua.gradsoft"

scalaVersion := "2.10.0"

crossScalaVersions := Seq("2.10.0")

version:="0.7.x-SNAPSHOT"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" intransitive()

libraryDependencies +=  "org.specs2" %% "specs2" % "1.14" intransitive()

// for examples:

libraryDependencies +=  "org.squeryl" %% "squeryl" % "0.9.5-6" % "test"

libraryDependencies +=  "com.h2database" % "h2" % "1.2.127" % "test"



publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://rssh.github.com/fixture-state-management</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>pt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:rssh/fixture-state-management.git</url>
    <connection>scm:git:git@github.com:rssh/fixture-state-management.git</connection>
  </scm>
  <developers>
    <developer>
      <id>rssh</id>
      <name>Ruslan Shevchenko</name>
      <url>rssh.github.com</url>
    </developer>
  </developers>
)

