package models

import java.util.Date

import collection.mutable.Stack
import org.scalatest._
import scalikejdbc._

/**
 * Test for Member
 */
class MemberSpec extends FlatSpec with Matchers {
  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should be(2)
    stack.pop() should be(1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a[NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    }
  }

  "Member" should "can create" in {
    DB.localTx { implicit session =>
      val now = new Date()
      val teamId = Team.insert(new Team(id = 0, name = "new team", createdAt = now))
      val team = Team.find(teamId).get
      val memberId = Member.insert(new Member(id = 0, name = "taro", teamId = teamId, createdAt = now, team = team))
      val member = Member.find(memberId).get
      member.name should be("taro")
    }
  }
}
