package models

import play.api.libs.json._
import scalikejdbc._
import utils.ReflectionSupport
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
 * Wrapper of ScalikeJDBC.
 *
 * With automatic insert, update, and find.
 */
trait AutoSQLSyntaxSupport[S] extends SQLSyntaxSupport[S] with ReflectionSupport {
  /**
   * if the primary key of the table is not "id", override this field.
   */
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
    // convert JsObject to String
    val filter = {o:Any =>
      if (o == null) {
        null
      } else if (o.isInstanceOf[JsObject]) {
        Json.toJson(o.asInstanceOf[JsObject]).toString()
      } else {
        o
      }
    }

    colNames.flatMap { cn =>
      if (valueMap.contains(cn)) {
        Some(cn, filter(valueMap(cn)))
      } else {
        val camel = toCamel(cn)
        if (valueMap.contains(camel)) {
          Some(cn, filter(valueMap(camel)))
        } else {
          None
        }
      }
    }
  }


  /**
   * insert and get new record
   */
  def insertGet[T: TypeTag : ClassTag](obj: T)(implicit session: DBSession): S = {
    get(insert(obj)).get
  }

  /**
   * insert new record
   * @param obj members are translated automatically, especially JsObject will be serialized into text.
   * @return new ID of inserted record
   */
  def insert[S: TypeTag : ClassTag](obj: S)(implicit session: DBSession): Long = {
    val cnValueList = columnValueList(obj)
    // separate if id > 0 field exists
    val nonZeroIdField = cnValueList.find { p =>
      p._1 == idColName && p._2.asInstanceOf[Long] > 0
    }
    val (cnValues, returnId:Long) = nonZeroIdField match {
      case Some(p) => (cnValueList, p._2.asInstanceOf[Long])
      case _ => (cnValueList.filterNot(_._1 == idColName), 0L)
    }

    val sql = SQL(
      s"""INSERT INTO ${tableName}
         (${cnValues.map { case (k, v) => k }.mkString(",")})
         VALUES
         (${cnValues.map(_ => "?").mkString(",")})""")
    val values = cnValues.map { case (k, v) => v }.toList
    val stt = sql.bind(values: _*)
    if (returnId == 0) {
      stt.updateAndReturnGeneratedKey.apply()
    } else {
      stt.update.apply()
      returnId
    }
  }

  /**
   * update existing record
   * @param obj members are translated automatically, especially JsObject will be serialized into text.
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

  /**
   * single record by id
   */
  def get(id: Long)(implicit session: DBSession = AutoSession): Option[S] = {
    select(Map(idColName -> id)).single.apply()
  }

  /**
   * single record by free condition
   * @param where Map with condition
   */
  def find(where:Map[String, Any] = Map())(implicit session: DBSession = AutoSession): Option[S] = {
    select(where).single.apply()
  }


  /**
   * records by free condition
   * @param where Map with condition
   */
  def list(where:Map[String, Any] = Map())(implicit session: DBSession = AutoSession): Seq[S] = {
    select(where).list.apply()
  }

  /**
   * Subclass must define select method.
   * @param where Map with condition
   */
  protected def select(where:Map[String, Any])(implicit session: DBSession = AutoSession): SQL[S, HasExtractor]

  /**
   * build where cruise
   * operator can be: >= <= = < > != <> like
   * @param where ("tableName.colName operator" -> value) all conditions are joined with "and".
   *              e.g. Map("name" -> "taro"), Map("count >" -> 10),
   *              Map("table.name like" -> "taro%", "count >" -> 4)
   * @return SQL condition
   */
  protected def buildWhereCruise(where:Map[String,Any]): (String, Seq[Any]) = {
    if (where.size == 0) {
      ("1 = 1", Nil)
    } else {
      val keyPattern = """(?i)(?:(\w+)\.)?(\w+)(?:\s+(\>=|\<=|=|\<|\>|\!=|\<\>|like))?""".r
      val lines = where.map{case (key, value) =>
        key match {
          case keyPattern(a, c, o) => {
            val tn = if (a == null) tableName else a
            val op = if (o == null) "=" else o
            (s"${tn}.${c} ${op} ?", value)
          }
          case _ => throw new Exception("invalid pattern:" + key)
        }
      }
      val cruise = lines.map{case (colOp, value) => colOp}.mkString(" and ")
      val values = lines.map{case (colOp, value) => value}.toList
      (cruise, values)
    }
  }
}



/**
 * Further support of SQLSyntaxSupport
 * @tparam S
 */
trait SingleAutoSQLSyntaxSupport[S] extends AutoSQLSyntaxSupport[S] {

  def apply(cols: ResultName[S])(rs: WrappedResultSet): S

  def apply(p: SyntaxProvider[S])(rs: WrappedResultSet): S = apply(p.resultName)(rs)

  /**
   * build select SQL
   * @param where
   * @param session
   * @return
   */
  def select(where:Map[String, Any])(implicit session: DBSession = AutoSession): SQL[S, HasExtractor] = {
    val t = this
    val tx = t.syntax
    val (cruise, params) = buildWhereCruise(where)
    SQL(s"""
          SELECT ${tx.result.*.value}
          FROM ${t.as(tx).value}
          WHERE ${cruise}
          ORDER BY ${idColName}
      """).bind(params:_*).map(t(tx))
  }
}

/**
 * Further support of SQLSyntaxSupport, of which the records are joined one to one.
 */
trait JoinedAutoSQLSyntaxSupport[S, T] extends AutoSQLSyntaxSupport[S] {
  /**
   * column name with which foreign table's key column joins
   */
  val joinColName: String
  /**
   * A companion object of the foreign table
   */
  val joinedCompanion: AutoSQLSyntaxSupport[T]

  def apply(cols: ResultName[S], tCols: ResultName[T])(rs: WrappedResultSet): S

  def apply(p: SyntaxProvider[S], tp: SyntaxProvider[T])(rs: WrappedResultSet): S = apply(p.resultName, tp.resultName)(rs)

  /**
   * build select SQL
   */
  def select(where:Map[String, Any])(implicit session: DBSession = AutoSession): SQL[S, HasExtractor] = {
    val (s, t) = (this, joinedCompanion)
    val (sx, tx) = (s.syntax, t.syntax)
    val (cruise, params) = buildWhereCruise(where)
    SQL(
      s"""
          SELECT ${sx.result.*.value}, ${tx.result.*.value}
          FROM ${s.as(sx).value}
            INNER JOIN ${t.as(tx).value}
            ON ${tx.tableAliasName}.${joinedCompanion.idColName} = ${sx.tableAliasName}.${joinColName}
          WHERE ${cruise}
          ORDER BY ${sx.tableAliasName}.${idColName}
      """).bind(params:_*).map(s(sx, tx))
  }


}


