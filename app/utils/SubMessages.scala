package utils

import play.api._
import play.api.i18n._
import play.api.Play.current
import play.utils.Resources


/**
 * Messages instance binded to subdirectory.
 * With this class, you can put "messages" file into sub directories.
 * for example.
 *
 * conf/
 *    sub1/
 *        messages
 *        messages.ja-JP
 *    sub2/
 *        messages
 *        messages.ja-JP
 * ```
 * implicit val messages = Messages.Implicits.applicationMessages
 * val sub1 = new SubMessages("sub1")
 * val message1 = sub1("message.key")
 * val sub2 = new SubMessages("sub2")
 * val message2 = sub2("message.key")
 * ```
 * this class uses the same language as current implicit Messages instance.
 */
class SubMessages(dirName:String) {
  /**
   * get resource message for key and params
   */
  def apply(key:String, params:Any*)(implicit messages:Messages):String = {
    implicit val lang = messages.lang
    messageApi(key, params:_*)
  }
  /**
   * get resource message for key and params
   */
  def apply(keys:Seq[String], params:Any*)(implicit messages:Messages):String = {
    implicit val lang = messages.lang
    messageApi(keys, params:_*)
  }

  /**
   *
   */
  private lazy val messageApi:MessagesApi = {
    val config = current.configuration
    val env = new Environment(new java.io.File("."), getClass.getClassLoader, current.mode)
    val langs = new DefaultLangs(config)
    new DefaultMessagesApi(env, config, langs) {
      /**
       * load messages file from subDirectory
       */
      override protected def loadMessages(file: String): Map[String, String] = {
        import scala.collection.JavaConverters._
        env.classLoader.getResources(dirName + "/" + file).asScala.toList
          .filterNot(url => Resources.isDirectory(env.classLoader, url)).reverse
          .map { messageFile =>
            Messages.parse(Messages.UrlMessageSource(messageFile), messageFile.toString).fold(e => throw e, identity)
          }.foldLeft(Map.empty[String, String]) { _ ++ _ }
      }
    }
  }


}


