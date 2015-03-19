package almhirt.i18n.text

import almhirt.common._
import com.ibm.icu.text.{ BreakIterator ⇒ UBreakIterator }
import almhirt.i18n.LocaleMagnet

object BreakIterator {
  def wordInstance[L: LocaleMagnet](text: String, locale: L): AlmIterator[Int] = {
    val uLoc = implicitly[LocaleMagnet[L]].toULocale(locale)
    val uIter = UBreakIterator.getWordInstance(uLoc)
    uIter.setText(text)
    var firstCalled = false
    AlmIterator.fromElementsProducer[Int] { () ⇒
      {
        if (firstCalled) {
          val next = uIter.next()
          if (next == UBreakIterator.DONE)
            None
          else
            Some(next)
        } else {
          firstCalled = true
          Some(uIter.first())
        }
      }
    }
  }

  def lineInstance[L: LocaleMagnet](text: String, locale: L): AlmIterator[Int] = {
    val uLoc = implicitly[LocaleMagnet[L]].toULocale(locale)
    val uIter = UBreakIterator.getLineInstance(uLoc)
    uIter.setText(text)
    var firstCalled = false
    AlmIterator.fromElementsProducer[Int] { () ⇒
      {
        if (firstCalled) {
          val next = uIter.next()
          if (next == UBreakIterator.DONE)
            None
          else
            Some(next)
        } else {
          firstCalled = true
          Some(uIter.first())
        }
      }
    }
  }

}