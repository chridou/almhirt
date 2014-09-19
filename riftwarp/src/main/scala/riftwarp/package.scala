import scala.language.implicitConversions
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

package object riftwarp {
//  implicit class HttpContentTypeOps(self: HttpContentType) {
//    def channelAndDescriptor(): AlmValidation[(String, Option[WarpDescriptor])] =
//      self.primary.split('+') match {
//        case Array(ident, ch) ⇒
//          self.options.get("version") match {
//            case Some(v) ⇒ v.toIntAlm.map(version ⇒ (ch, Some(WarpDescriptor(ident, version))))
//            case None ⇒ (ch, Some(WarpDescriptor(ident))).success
//          }
//        case Array(ch) ⇒ (ch, None).success
//        case x ⇒ UnspecifiedProblem(s""""${self.toString}" is not a suitable HttpContentTYpe for use with RiftWarp.""").failure
//      }
//  }
  implicit def warpChannel2String(ch: WarpChannel): String = ch.channelDescriptor
}