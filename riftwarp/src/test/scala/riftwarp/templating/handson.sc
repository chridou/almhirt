package riftwarp.templating

import shapeless._
object handson {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  case class AA(a: String, b: Int)
  
  val x = !StringPart[AA]("a", _.a) ~ (IntPart[AA]("b", _.b)) ~ (StringPart[AA]("c", _.a))
                                                  //> x  : riftwarp.templating.Template[shapeless.::[riftwarp.templating.StringPar
                                                  //| t[riftwarp.templating.handson.AA],shapeless.::[riftwarp.templating.IntPart[r
                                                  //| iftwarp.templating.handson.AA],shapeless.::[riftwarp.templating.StringPart[r
                                                  //| iftwarp.templating.handson.AA],shapeless.HNil]]],riftwarp.templating.handson
                                                  //| .AA] = riftwarp.templating.Template$$anon$1@5cef8a56
        
        
           
  x()                                             //> res0: shapeless.::[riftwarp.templating.StringPart[riftwarp.templating.handso
                                                  //| n.AA],shapeless.::[riftwarp.templating.IntPart[riftwarp.templating.handson.A
                                                  //| A],shapeless.::[riftwarp.templating.StringPart[riftwarp.templating.handson.A
                                                  //| A],shapeless.HNil]]] = riftwarp.templating.StringPart@71811419 :: riftwarp.t
                                                  //| emplating.IntPart@514eaf86 :: riftwarp.templating.StringPart@4e5b01e :: HNil
                                                  //| 
  x().tupled                                      //> res1: (riftwarp.templating.StringPart[riftwarp.templating.handson.AA], riftw
                                                  //| arp.templating.IntPart[riftwarp.templating.handson.AA], riftwarp.templating.
                                                  //| StringPart[riftwarp.templating.handson.AA]) = (riftwarp.templating.StringPar
                                                  //| t@71811419,riftwarp.templating.IntPart@514eaf86,riftwarp.templating.StringPa
                                                  //| rt@4e5b01e)
  val a = AA("a", 1)                              //> a  : riftwarp.templating.handson.AA = AA(a,1)
  x.hlist.head                                    //> res2: riftwarp.templating.StringPart[riftwarp.templating.handson.AA] = riftw
                                                  //| arp.templating.StringPart@71811419
  //x.hlist.tail.head

 // val z = x().map(evalPolyF)
  
  val gen = x.genElemsFun                         //> gen  : (riftwarp.templating.handson.AA, riftwarp.WarpPackers) => almhirt.com
                                                  //| mon.AlmValidation[Seq[riftwarp.WarpElement]] = <function2>
  gen(a, null)                                    //> res3: almhirt.common.AlmValidation[Seq[riftwarp.WarpElement]] = Success(List
                                                  //| (WarpElement(a,Some(WarpString(a))), WarpElement(b,Some(WarpInt(1))), WarpEl
                                                  //| ement(c,Some(WarpString(a)))))
  
  val r = 1 :: "w" :: 1L :: HNil                  //> r  : shapeless.::[Int,shapeless.::[String,shapeless.::[Long,shapeless.HNil]]
                                                  //| ] = 1 :: w :: 1 :: HNil
  r.head                                          //> res4: Int = 1
  r.tail.head                                     //> res5: String = w
  
                            
	object eval extends Poly1 {
	  implicit def caseString = at[String](_.length)
	  implicit def caseInt = at[Int](_.toString+"!")
	  implicit def caseLong = at[Long](_ => "Long!")
	}
	
	r.map(eval)                               //> res6: shapeless.::[String,shapeless.::[Int,shapeless.::[String,shapeless.HNi
                                                  //| l]]] = 1! :: 1 :: Long! :: HNil
  val y = r.tupled                                //> y  : (Int, String, Long) = (1,w,1)
}