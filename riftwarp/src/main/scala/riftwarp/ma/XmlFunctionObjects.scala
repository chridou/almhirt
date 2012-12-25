package riftwarp.ma

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import scala.xml.{ Elem, Text, TopScope, Null, UnprefixedAttribute }

trait XmlElemFolder extends RegisterableChannelFolder[Elem, Elem] {
  val channel = RiftXml()
  val tA = classOf[Elem]
  val tB = classOf[Elem]
  def fold[M[_]](ma: M[Elem])(funcObj: MAFunctions[M]): AlmValidation[Elem] = {
    if (funcObj.isEmpty(ma)) {
      Elem(null, "Elements", Null, TopScope).success
    } else {
      funcObj match {
        // Since it is automatically looked up, it should be the right thing...
        case fo: LinearMAFunctions[M] =>
          Elem(null, "Elements", Null, TopScope, fo.toList(ma): _*).success
        case _ =>
          UnspecifiedProblem("Not yet supported").failure
      }
    }
  }
}

object XmlElemFolder extends XmlElemFolder

