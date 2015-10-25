package utils

import java.util.Date

import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}
import ru._

/**
 *
 */
trait ReflectionSupport {
  /**
   * Get Type from any Object
   */
  def getType[A: TypeTag](obj: A): Type = typeOf[A]

  /**
   * Get RuntimeMirror from currentThread
   */
  private def mirror = runtimeMirror(Thread.currentThread.getContextClassLoader)

  /**
   * Returns property name and value of a case class instance
   */
  def caseObjectToMap[T: TypeTag : ClassTag](obj: T): Map[String, Any] = {
    val insMirror = mirror.reflect(obj)
    val tp = typeOf[T]
    tp.members.collect {
      case m: MethodSymbol if m.isCaseAccessor => {
        val name = m.name.toString
        val value = insMirror.reflectMethod(m)()
        (name, value)
      }
    }.toMap
  }
}
