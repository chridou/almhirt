package almhirt.i18n.text

import org.scalatest._

class WordIteratorTests extends FunSuite with Matchers {
  test("""It should deliver [] for  """"") {
    val iter = WordIterator("", "de")
    iter.toList should equal(Nil)
  }

  test("""It should deliver [a] for  "a"""") {
    val iter = WordIterator("a", "de")
    iter.toList should equal(List("a"))
  }

  test("""It should deliver ["a", "bc"] for  "a bc"""") {
    val iter = WordIterator("a bc", "de")
    iter.toList should equal(List("a", "bc"))
  }

  test("""It should deliver ["a", "bc"] for  "a bc."""") {
    val iter = WordIterator("a bc.", "de")
    iter.toList should equal(List("a", "bc"))
  }

  test("""It should deliver ["The", "quick","brown","open", "minded","fox","jumped","over","the","fence"] for "The quick brown open-minded fox jumped over the fence."""") {
    val iter = WordIterator("The quick brown open-minded fox jumped over the fence.", "en")
    iter.toList should equal(List("The", "quick", "brown", "open", "minded", "fox", "jumped", "over", "the", "fence"))
  }
}
