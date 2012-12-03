package riftwarp.ext.liftjson

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.ma._
import net.liftweb.json._


trait LiftJsonFolder extends RegisterableChannelFolder[JValue, JArray] {
  val channel = RiftJson()
  val tA = classOf[JValue]
  val tB = classOf[JArray]
  def fold[M[_]](ma: M[JValue])(funcObj: MAFunctions[M]): AlmValidation[JArray] = {
    if (funcObj.isEmpty(ma)) {
      JArray(List.empty).success
    } else {
      if (funcObj.hasLinearCharacteristics) {
        JArray(funcObj.toList(ma)).success
      } else {
        UnspecifiedProblem("Not yet supported").failure
      }
    }
  }
}

object LiftJsonFolder extends LiftJsonFolder