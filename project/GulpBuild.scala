import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys._

/**
 * Build for Gulp in compile
 */
object GulpBuild {

  val gulpBuild = TaskKey[Unit]("gulp-build")

  val settings = Seq(

    // add "gulp" commands in sbt
    commands <++= baseDirectory { base => Seq(Gulp.command(base))},

    gulpBuild := {
      val result = Gulp.process(baseDirectory.value, "--type", "build", "clean", "compile").run().exitValue()
      if (result != 0) {
        System.exit(result)
      }
    },

    // runs gulp before dist the application
    dist <<= dist dependsOn (gulpBuild)
  )
}
