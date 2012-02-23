
name:="fixture-state-management"

version:="0.2.0-SNAPSHOT"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" 

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
  <url>http://www.cakesolutions.org/specs2-spring.html</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:janm399/specs2-spring.git</url>
    <connection>scm:git:git@github.com:janm399/specs2-spring.git</connection>
  </scm>
  <developers>
    <developer>
      <id>janmachacek</id>
      <name>Jan Machacek</name>
      <url>http://cakesolutions.org</url>
    </developer>
  </developers>
)

