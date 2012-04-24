
name:="fixture-state-management"

organization:="ua.gradsoft"

crossScalaVersions := Seq("2.9.1","2.9.2")

version:="0.5.0-SNAPSHOT"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" intransitive()

libraryDependencies +=  "org.specs2" %% "specs2" % "1.9" intransitive()

// for examples:

libraryDependencies +=  "org.squeryl" %% "squeryl" % "0.9.5" % "test"

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
      <url>http://www.gradsoft.ua</url>
    </developer>
  </developers>
)

