package almhirt.args

import org.scalatest._
import scalaz.Success

class ArgsFunsTests extends FunSuite with Matchers {
  val innerBC = Map("A" -> "peter", "B" -> 1L)

  val innerB = Map("A" -> "mary", "B" -> 3.0, "C" -> innerBC)

  val testMap = Map("A" -> "heinz", "B" -> innerB, "C" -> Vector(1, 2, 3))

  test("""getValue[String] must return a Success("heinz") for key "A"""") {
    funs.getValue[String]("A", testMap) should equal(Success("heinz"))
  }

  test("""getValue[Int] must fail for key "A"""") {
    funs.getValue[Int]("A", testMap).isFailure should be(true)
  }

  test("""getValue[Vector[Int]] must return a Success(Vector(1,2,3)) for key "C"""") {
    funs.getValue[Vector[Int]]("C", testMap) should equal(Success(Vector(1, 2, 3)))
  }

  test("""getValue[List[Int]] must fail for key "C"""") {
    funs.getValue[List[Int]]("C", testMap).isFailure should be(true)
  }

  test("""getValueFromPropertyPath[String] must return a Success("heinz") for path "A"""") {
    funs.getValueFromPropertyPath[String]("A", testMap) should equal(Success("heinz"))
  }

  test("""getValueFromPropertyPath[String] must return a Success("mary") for path "B.A"""") {
    funs.getValueFromPropertyPath[String]("B.A", testMap) should equal(Success("mary"))
  }

  test("""getValueFromPropertyPath[String] must return a Success("peter") for path "B.C.A"""") {
    funs.getValueFromPropertyPath[String]("B.C.A", testMap) should equal(Success("peter"))
  }
  

  test("""getValueFromPropertyPath[String] must fail for path "B.B.A"""") {
    funs.getValueFromPropertyPath[String]("B.B.A", testMap).isFailure should be(true)
  }

  test("""getValueFromPropertyPath[String] must fail for path "B.X.A"""") {
    funs.getValueFromPropertyPath[String]("B.X.A", testMap).isFailure should be(true)
  }
  
  test("""getValueFromPropertyPath[Int] must fail for path "B.C.A"""") {
    funs.getValueFromPropertyPath[Int]("B.C.A", testMap).isFailure should be(true)
  }
  
}