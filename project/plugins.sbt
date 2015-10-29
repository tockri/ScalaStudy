// Comment to get more information during initialization
logLevel := Level.Warn

// Repositories
resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

// SBT Plugins
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

// Support for using S3
addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.5.0")
