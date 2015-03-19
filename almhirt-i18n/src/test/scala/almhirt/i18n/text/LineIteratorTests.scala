package almhirt.i18n.text

import org.scalatest._

class LineIteratorTests extends FunSuite with Matchers {
  test("""It should deliver [] for  """"") {
    val iter = LineIterator("", "de")
    iter.toList should equal(Nil)
  }

  test("""It should deliver [a] for  "a"""") {
    val iter = LineIterator("a", "de")
    iter.toList should equal(List("a"))
  }

  test("""It should deliver ["a ", "bc"] for  "a bc"""") {
    val iter = LineIterator("a bc", "de")
    iter.toList should equal(List("a", "bc"))
  }

  test("""It should deliver ["a ", "bc."] for  "a bc."""") {
    val iter = LineIterator("a bc.", "de")
    iter.toList should equal(List("a", "bc"))
  }

  test("""It should deliver ["The", "quick","brown","open-minded","fox","jumped","over","the","fence"] for "The quick brown open-minded fox jumped over the fence."""") {
    val iter = LineIterator("The quick brown open-minded fox jumped over the fence.", "en")
    iter.toList should equal(List("The", "quick", "brown", "open-minded", "fox", "jumped", "over", "the", "fence"))
  }

}