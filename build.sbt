
import PlayKeys._

name := "ScalaTest"

version := "1.0.0"

lazy val root = (project in file("."))
.enablePlugins(PlayScala)
.settings(
  scalaVersion := "2.11.7",
  resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/",
  resolvers += "gcm-server-repository" at "https://raw.github.com/slorber/gcm-server-repository/master/releases/",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  sources in (Compile, doc) := Nil,
  publishArtifact in (Compile, packageDoc) := false,
  javaOptions in Test ++= Seq(
    "-Dconfig.file=conf/test.conf",
    "-Dlogger.file=conf/test-logger.xml"
  ),
  javaOptions in Test ++= sys.process.javaVmArguments.filter(
    a => Seq("-Xmx","-Xms","-XX").exists(a.startsWith)
  ),
  parallelExecution in Test := false
)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  evolutions,
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalikejdbc" %% "scalikejdbc" % "2.2.8" excludeAll(
    ExclusionRule(organization = "commons-dbcp", name="commons-dbcp")
  ),
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "2.2.8",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.2.8",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.4.2",
  "org.scalikejdbc" %% "scalikejdbc-play-dbapi-adapter" % "2.4.2",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41"
)



playRunHooks <+= baseDirectory.map(base => Gulp(base))

GulpBuild.settings
