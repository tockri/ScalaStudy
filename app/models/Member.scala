package models

import java.util.Date
import scalikejdbc._

/**
 * model for team table
 */
case class Team(id: Long, name: String, createdAt: Date)

object Team extends AutoSQLSyntaxSupport[Team] {
  /**
   * convert from record
   */
  def apply(t: ResultName[Team])(rs: WrappedResultSet): Team = {
    new Team(
      id = rs.long(t.id),
      name = rs.string(t.name),
      createdAt = rs.date(t.createdAt)
    )
  }
}


/**
 * model for user table
 */
case class Member(id: Long, name: String, teamId: Long, createdAt: Date, team: Team)


object Member extends AutoJoinSQLSyntaxSupport[Member, Team] {
  override val joinColName = "team_id"
  override val joinedCompanion = Team
  /**
   * convert from record
   */
  def apply(m: ResultName[Member], t: ResultName[Team])(rs: WrappedResultSet): Member = {
    new Member(
      id = rs.long(m.id),
      name = rs.string(m.name),
      teamId = rs.long(m.teamId),
      createdAt = rs.date(m.createdAt),
      team = Team(t)(rs)
    )
  }

}
