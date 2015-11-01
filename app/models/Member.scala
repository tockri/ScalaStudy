package models

import java.util.Date
import scalikejdbc._

/**
 * model for team table
 */
case class Bizteam(id: Long, name: String, createdAt: Date)

object Bizteam extends SingleAutoSQLSyntaxSupport[Bizteam] {
  /**
   * convert from record
   */
  def apply(t: ResultName[Bizteam])(rs: WrappedResultSet): Bizteam = {
    new Bizteam(
      id = rs.long(t.id),
      name = rs.string(t.name),
      createdAt = rs.date(t.createdAt)
    )
  }
}


/**
 * model for user table
 */
case class Member(id: Long, name: String, bizteamId: Long, createdAt: Date, team: Bizteam)


object Member extends JoinedAutoSQLSyntaxSupport[Member, Bizteam] {
  override val joinColName = "bizteam_id"
  override val joinedCompanion = Bizteam
  /**
   * convert from record
   */
  def apply(m: ResultName[Member], t: ResultName[Bizteam])(rs: WrappedResultSet): Member = {
    new Member(
      id = rs.long(m.id),
      name = rs.string(m.name),
      bizteamId = rs.long(m.bizteamId),
      createdAt = rs.date(m.createdAt),
      team = Bizteam(t)(rs)
    )
  }

}
