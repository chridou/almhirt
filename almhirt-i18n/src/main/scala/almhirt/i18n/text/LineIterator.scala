package almhirt.i18n.text

import almhirt.common._
import almhirt.i18n.LocaleMagnet

object LineIterator {
  def apply[L: LocaleMagnet](text: String, locale: L): AlmIterator[String] = {
    val iter = BreakIterator.lineInstance(text, locale)

    var last: Option[Int] = iter.next()
    val producer: () ⇒ Option[String] = () ⇒ {
      last match {
        case None ⇒
          None
        case Some(begin) ⇒
          last = iter.next()
          last match {
            case Some(end) ⇒
              Some(text.substring(begin, end))
            case None ⇒
              None
          }
      }

    }
    AlmIterator.fromElementsProducer { producer }
  }
}