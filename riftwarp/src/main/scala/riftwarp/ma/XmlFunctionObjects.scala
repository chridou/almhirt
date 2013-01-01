package riftwarp.ma

import language.higherKinds

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
      Elem(null, "Collection", Null, TopScope, true).success
    } else {
      funcObj match {
        // Since it is automatically looked up, it should be the right thing...
        case fo: LinearMAFunctions[M] =>
          Elem(null, "Collection", Null, TopScope, true, fo.toList(ma): _*).success
        case _ =>
          UnspecifiedProblem("Not yet supported").failure
      }
    }
  }
}

object XmlElemFolder extends XmlElemFolder

