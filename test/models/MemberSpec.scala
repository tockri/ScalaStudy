package models

import java.util.Date

import org.scalatest._
import play.api.test.FakeApplication
import play.api.test.Helpers
import scalikejdbc._
import org.scalatestplus.play.OneAppPerTest

/**
 * Test for Member
 */
class MemberSpec extends WordSpec with OptionValues with MustMatchers with OneAppPerTest {
  override def newAppForTest(testData:TestData) = FakeApplication(additionalConfiguration = Helpers.inMemoryDatabase())

  "Member" should {
    "create new record" in {
      DB.localTx { implicit session =>
        val now = new Date()
        val teamId = Team.insert(new Team(id = 0, name = "new team", createdAt = now))
        val team = Team.get(teamId).value
        team.name mustEqual "new team"
        val memberId = Member.insert(new Member(id = 0, name = "taro", teamId = teamId, createdAt = now, team = team))
        val member = Member.get(memberId).value
        member.name mustEqual "taro"
      }
    }
  }
}
