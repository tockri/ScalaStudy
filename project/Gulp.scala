import play.PlayRunHook
import sbt._

import java.net.InetSocketAddress

object Gulp {

  def apply(base: File): PlayRunHook = {

    object GulpProcess extends PlayRunHook {

      var process: Option[Process] = None

      override def beforeStarted(): Unit = {
        process = Some(Gulp.process(base).run)
      }

      override def afterStarted(addr: InetSocketAddress): Unit = {
      }

      override def afterStopped(): Unit = {
        process.map(p => p.destroy())
        process = None
      }
    }

    GulpProcess
  }

  private def gulpCommand = if (isWindows) "gulp.cmd" else "gulp"

  private val isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0

  def command(base: File) = Command.args("gulp", "<gulp-command>") { (state, args) =>
    process(base, args: _*).run
    state
  }

  def process(base: File, args: String*) = Process(gulpCommand :: args.toList, base)
}
