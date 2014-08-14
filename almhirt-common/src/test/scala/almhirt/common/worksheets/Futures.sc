package almhirt.common.worksheets

import scala.concurrent.duration._
import scalaz.syntax.validation._
import almhirt.common._

object Futures {
  def printThreadName(t: String) = println(s"$t⇒${Thread.currentThread().getName()}")
                                                  //> printThreadName: (t: String)Unit
  
  
  implicit val execContext = HasExecutionContext.cached
                                                  //> execContext  : almhirt.common.HasExecutionContext = almhirt.common.HasExecut
                                                  //| ionContext$$anon$2@45fb302d
  implicit val atMost = FiniteDuration(1, "s")    //> atMost  : scala.concurrent.duration.FiniteDuration = 1 second
  
  printThreadName("")                             //> ⇒main

	AlmFuture{ printThreadName("A"); ().success }
                                                  //> res0: almhirt.common.AlmFuture[Unit] = almhirt.common.AlmFuture@6611d7b8
  var xx = 0                                      //> xx  : Int = 0

	val res1 =
		AlmFuture{ printThreadName("A"); xx = xx + 2; 2.success }
		.mapV{x ⇒ printThreadName("B"); xx = xx + 4 ;(x+4).success }
		.map{x ⇒ printThreadName("C"); xx = xx * 3; x * 3}.awaitResult
                                                  //> A⇒pool-1-thread-1
                                                  //| A⇒pool-1-thread-2
                                                  //| B⇒pool-1-thread-3
                                                  //| C⇒pool-1-thread-2
                                                  //| res1  : almhirt.common.AlmValidation[Int] = Success(18)
  println(xx)                                     //> 18

	val res2 =
		AlmFuture.successful{ printThreadName("A"); 2 }
		.mapV{x ⇒ printThreadName("B"); (x+4).success }
		.map{x ⇒ printThreadName("C"); x * 3}
		.flatMap{x ⇒ printThreadName("D"); AlmFuture{printThreadName("E"); (x * 3).success}}
		.map{x ⇒ printThreadName("F"); x * 3}
		.awaitResult                      //> A⇒main
                                                  //| B⇒pool-1-thread-2
                                                  //| C⇒pool-1-thread-2
                                                  //| D⇒pool-1-thread-2
                                                  //| E⇒pool-1-thread-3
                                                  //| F⇒pool-1-thread-3
                                                  //| res2  : almhirt.common.AlmValidation[Int] = Success(162)


  println("End")                                  //> End|
}