
resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

// see  https://groups.google.com/forum/#!msg/simple-build-tool/BrV2OiVS1FU/h_Eu47hxeiQJ
// TODO: enable if here when appropriative sbt bug will be fixed.
//addSbtPlugin("com.typesafe.sbt" % "sbt-gpg" % "0.8")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.0")
