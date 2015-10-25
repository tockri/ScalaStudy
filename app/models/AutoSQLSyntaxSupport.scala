package models

import scalikejdbc._
import utils.ReflectionSupport
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._



trait AutoSQLBase[S] extends SQLSyntaxSupport[S] with ReflectionSupport {
  val idColName = "id"

  /**
   * snake_case to camelCase
   * @param s snake_case
   * @return camelCase
   */
  private def toCamel(s: String): String = {
    val split = s.split("_")
    val tail = split.tail.map { x => x.head.toUpper + x.tail }
    split.head + tail.mkString
  }

  /**
   * List[(columnName -> value)]
   */
  private def columnValueList[S: TypeTag : ClassTag](obj: S): Seq[(String, Any)] = {
    val colNames = syntax.columns.map(_.value)
    val valueMap = caseObjectToMap(obj)
    colNames.map { cn => (cn,
      if (valueMap.contains(cn)) {
        valueMap(cn)
      } else {
        val camel = toCamel(cn)
        if (valueMap.contains(camel)) {
          valueMap(camel)
        } else {
          null
        }
      })
    }
  }

  /**
   * insert new record
   * @param obj
   * @return new ID of inserted record
   */
  def insert[S: TypeTag : ClassTag](obj: S)(implicit session: DBSession): Long = {
    val cnValues = columnValueList(obj).filter(_._1 != idColName)
    val sql = SQL(
      s"""INSERT INTO ${tableName}
         (${cnValues.map { case (k, v) => k }.mkString(",")})
         VALUES
         (${cnValues.map(_ => "?").mkString(",")})""")
    val values = cnValues.map { case (k, v) => v }.toList
    sql.bind(values: _*).updateAndReturnGeneratedKey.apply()
  }

  /**
   * update existing record
   */
  def update[S: TypeTag : ClassTag](obj: S)(implicit session: DBSession): Unit = {
    val cnValues = columnValueList(obj)
    cnValues.find(_._1 == idColName).map { case (cn, id) if (id.asInstanceOf[Long] > 0) =>
      val setValues = cnValues.filter(_._1 != idColName)
      val setPart = setValues.map { case (k, v) => k + "=?" }.mkString(",")
      val values = setValues.map { case (k, v) => v } ++ Seq(id)
      SQL(
        s"""UPDATE ${tableName}
            SET ${setPart}
            WHERE ${idColName} = ?"""
      ).bind(values: _*).update.apply()
    }
  }
}

/**
 * Further support of SQLSyntaxSupport
 * @tparam S
 */
trait AutoSQLSyntaxSupport[S] extends AutoSQLBase[S] {

  def apply(cols: ResultName[S])(rs: WrappedResultSet): S

  def apply(p: SyntaxProvider[S])(rs: WrappedResultSet): S = apply(p.resultName)(rs)

  /**
   * single record
   */
  def find(id: Long)(implicit session: DBSession): Option[S] = {
    val t = this
    val tx = t.syntax
    SQL(
      s"""
          SELECT ${tx.result.*.value}
          FROM ${t.as(tx).value}
          WHERE ${tx.tableAliasName}.${idColName} = ${id}
      """).map(t(tx)).single.apply()
  }

  /**
   * all records
   */
  def list()(implicit session: DBSession): Seq[S] = {
    val t = this
    val tx = t.syntax
    sql"""
          SELECT ${tx.result.*}
          FROM ${t.as(tx)}
      """.map(t(tx)).list.apply()
  }


}

/**
 * Further support of SQLSyntaxSupport, of which the records are joined one to one.
 */
trait AutoJoinSQLSyntaxSupport[S, T] extends AutoSQLBase[S] {

  val joinColName: String
  val joinedCompanion: AutoSQLBase[T]

  def apply(cols: ResultName[S], tCols: ResultName[T])(rs: WrappedResultSet): S

  def apply(p: SyntaxProvider[S], tp: SyntaxProvider[T])(rs: WrappedResultSet): S = apply(p.resultName, tp.resultName)(rs)

  /**
   * single record
   */
  def find(id: Long)(implicit session: DBSession = AutoSession): Option[S] = {
    val (s, t) = (this, joinedCompanion)
    val (sx, tx) = (s.syntax, t.syntax)
    SQL(
      s"""
          SELECT ${sx.result.*.value}, ${tx.result.*.value}
          FROM ${s.as(sx).value}
            INNER JOIN ${t.as(tx).value}
            ON ${tx.tableAliasName}.${joinedCompanion.idColName} = ${sx.tableAliasName}.${joinColName}
          WHERE ${sx.tableAliasName}.${idColName} = ${id}
      """).map(s(sx, tx)).single.apply()
  }

  /**
   * all records
   */
  def list()(implicit session: DBSession = AutoSession): Seq[S] = {
    val (s, t) = (this, joinedCompanion)
    val (sx, tx) = (s.syntax, t.syntax)
    SQL(
      s"""
          SELECT ${sx.result.*.value}, ${tx.result.*.value}
          FROM ${s.as(sx).value}
            INNER JOIN ${t.as(tx).value}
            ON ${tx.tableAliasName}.${joinedCompanion.idColName} = ${sx.tableAliasName}.${joinColName}
      """).map(s(sx, tx)).list.apply()
  }
}

