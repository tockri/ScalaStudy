package controllers

import java.util.Date

import models._
import play.api._
import play.api.mvc._
import scalikejdbc._
import utils._

object Application extends Controller with ReflectionSupport {
  def index = Action {
    Ok("hello world!!")
  }

  def list = Action {
    DB.localTx {implicit session:DBSession =>
      val list = Member.list()
      Ok(views.html.index(list)("一覧画面"))
    }
  }

  def test = Action {
    val message = new StringBuilder()
    Member.find(1).map{ member =>
      message.append(member.name + "-" + member.team.name)
      message.append("\n-------\n")
      val map = caseObjectToMap(member)
      message.append(map.mkString("\n--\n"))
      message.append("\n\n")
      DB.localTx { implicit session =>
        message.append(Team.insert(member.team))

        Team.update(member.team.copy(name = "tested", createdAt = new Date(), id = 5))
        val newMember = Member.insert(member.copy(createdAt = new Date()))
        Member.update(member.copy(id = newMember, name = "updated"))
      }
    }
    Ok(message.toString())
  }
}
