package utils


import org.scalatest._
import play.api.i18n.{Messages, Lang}
import play.api.test.FakeApplication
import play.api.test.Helpers
import org.scalatestplus.play.OneAppPerTest
import Messages.Implicits._


/**
 * MessageSupport
 */
class MessageSupportSpec extends WordSpec with OptionValues with MustMatchers with OneAppPerTest {
  override def newAppForTest(testData:TestData) = FakeApplication(additionalConfiguration = Helpers.inMemoryDatabase())


  "MessageSupport" should {
    "return resource in English" in {
      val sm:SubMessages = new SubMessages("integrations/github")
      implicit val lang:Lang = Lang("en-US")
      sm("integration.github.summary") mustEqual "Get notifications for events such as push and pull requests to your repository."
      sm("integration.github.commits", 10) mustEqual "10 commit(s)"
      Messages("month.1") mustEqual "January"
    }

    "return first found string in keys" in {
      val sm:SubMessages = new SubMessages("integrations/github")
      implicit val lang:Lang = Lang("en-US")
      sm(Seq("integration.github.commits","foobar"), 10) mustEqual "10 commit(s)"
      sm(Seq("barbaz", "integration.github.commits","foobar"), 10) mustEqual "10 commit(s)"
    }


    "return resource in Japanese" in {
      val sm:SubMessages = new SubMessages("integrations/github")
      implicit val lang:Lang = Lang("ja-JP")
      sm("integration.github.summary") mustEqual "リポジトリへのプッシュやプルリクエストなどのイベントをTypetalkに通知できるようになります。"
      sm("integration.github.commits", 10) mustEqual "10個のコミット"
      Messages("month.1") mustEqual "睦月"
    }

    "change languages in one instance" in {
      val sm:SubMessages = new SubMessages("integrations/github")
      implicit var lang:Lang = Lang("ja-JP")
      sm("integration.github.summary") mustEqual "リポジトリへのプッシュやプルリクエストなどのイベントをTypetalkに通知できるようになります。"
      Messages("month.1") mustEqual "睦月"


      lang = Lang("en-US")
      sm("integration.github.summary") mustEqual "Get notifications for events such as push and pull requests to your repository."
      Messages("month.1") mustEqual "January"
    }

  }

}
