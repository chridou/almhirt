package riftwarp.worksheets

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.impl.rematerializers._

object JsonRematerializer {
  import riftwarp.inst._
  val orig = DimensionStdLibJson(List(1, "A", 1.89))
                                                  //> orig  : riftwarp.DimensionStdLibJson = DimensionStdLibJson(List(1, A, 1.89))
                                                  //| 
  val remat = new FromStdLibJsonRematerializer    //> remat  : riftwarp.impl.rematerializers.FromStdLibJsonRematerializer = riftwa
                                                  //| rp.impl.rematerializers.FromStdLibJsonRematerializer@668bbdab
  
//  val s: Set[Int] = List("Paris", "London").map(_.length)


  val res1: AlmValidation[Vector[Any]] = remat.collectionOfReprFromRepr(orig.manifestation)
                                                  //> res1  : almhirt.common.AlmValidation[Vector[Any]] = Success(Vector(1, A, 1.8
                                                  //| 9))

  val res2: AlmValidation[List[Any]] = remat.resequencedMappedFromRepr(orig.manifestation, x ⇒ x.success)
                                                  //> res2  : almhirt.common.AlmValidation[List[Any]] = Success(List(1, A, 1.89))
                                                  //| 
            
            
}