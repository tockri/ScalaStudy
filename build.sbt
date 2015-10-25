scalaVersion := "2.11.7"

lazy val root = Project("root", file("."))
  .enablePlugins(PlayScala)
  .settings(
)
resolvers ++= Seq(
  "sonatype releases" at "http://oss.sonatype.org/content/repositories/releases",
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  evolutions,
  "org.scalikejdbc"      %% "scalikejdbc"            % scalikejdbcVersion,
  "org.scalikejdbc"      %% "scalikejdbc-config"            % scalikejdbcVersion,
  "org.scalikejdbc"      %% "scalikejdbc-play-initializer"  % scalikejdbcPlayVersion,
  "org.scalikejdbc"      %% "scalikejdbc-play-dbapi-adapter" % scalikejdbcPlayVersion,
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

lazy val scalikejdbcVersion = "2.2.+"
lazy val scalikejdbcPlayVersion = "2.4.+"

