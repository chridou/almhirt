package almhirt.i18n.text

import org.scalatest._

class WordBreakIteratorTests extends FunSuite with Matchers {

  test("""It should deliver [0] for  """"") {
    val iter = BreakIterator.wordInstance("", "de")
    iter.toList should equal(List(0))
  }

  test("""It should deliver [0,1] for  "a"""") {
    val iter = BreakIterator.wordInstance("a", "de")
    iter.toList should equal(List(0, 1))
  }

  test("""It should deliver [0,1,2,4] for  "a bc"""") {
    val iter = BreakIterator.wordInstance("a bc", "de")
    iter.toList should equal(List(0, 1, 2, 4))
  }

}