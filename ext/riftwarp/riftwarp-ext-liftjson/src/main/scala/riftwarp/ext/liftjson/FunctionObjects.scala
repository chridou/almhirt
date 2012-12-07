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
      funcObj match {
        // Since it is automatically looked up, it should be the right thing...
        case fo: LinearMAFunctions[M] =>
          JArray(fo.toList(ma)).success
        case x =>
          UnspecifiedProblem("LiftJsonFolder:fold Not yet supported for '%s'".format(x)).failure
      }
    }
  }
}

object LiftJsonFolder extends LiftJsonFolder