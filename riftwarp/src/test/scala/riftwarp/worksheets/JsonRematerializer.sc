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
                                                  //| rp.impl.rematerializers.FromStdLibJsonRematerializer@379c6fd2
  
//  val s: Set[Int] = List("Paris", "London").map(_.length)



  val res: AlmValidation[List[Any]] = remat.getResequenced(orig.manifestation, x => x.success)
                                                  //> res  : almhirt.common.AlmValidation[List[Any]] = Success(List(1, A, 1.89))
}