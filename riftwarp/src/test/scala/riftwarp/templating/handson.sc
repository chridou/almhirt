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
                                                  //| .AA] = riftwarp.templating.Template$$anon$1@1b8acf07
        
        
           
  x()                                             //> res0: shapeless.::[riftwarp.templating.StringPart[riftwarp.templating.handso
                                                  //| n.AA],shapeless.::[riftwarp.templating.IntPart[riftwarp.templating.handson.A
                                                  //| A],shapeless.::[riftwarp.templating.StringPart[riftwarp.templating.handson.A
                                                  //| A],shapeless.HNil]]] = riftwarp.templating.StringPart@417e9329 :: riftwarp.t
                                                  //| emplating.IntPart@5cef8a56 :: riftwarp.templating.StringPart@726343c4 :: HNi
                                                  //| l
  x().tupled                                      //> res1: (riftwarp.templating.StringPart[riftwarp.templating.handson.AA], riftw
                                                  //| arp.templating.IntPart[riftwarp.templating.handson.AA], riftwarp.templating.
                                                  //| StringPart[riftwarp.templating.handson.AA]) = (riftwarp.templating.StringPar
                                                  //| t@417e9329,riftwarp.templating.IntPart@5cef8a56,riftwarp.templating.StringPa
                                                  //| rt@726343c4)
  val a = AA("a", 1)                              //> a  : riftwarp.templating.handson.AA = AA(a,1)
  x.hlist.head                                    //> res2: riftwarp.templating.StringPart[riftwarp.templating.handson.AA] = riftw
                                                  //| arp.templating.StringPart@417e9329
  //x.hlist.tail.head

 // val z = x().map(evalPolyF)
  
  val gen = x.genElemsFun                         //> gen  : (riftwarp.templating.handson.AA, riftwarp.WarpPackers) => almhirt.com
                                                  //| mon.AlmValidation[Seq[riftwarp.WarpElement]] = <function2>
  gen(a, null)                                    //> res3: almhirt.common.AlmValidation[Seq[riftwarp.WarpElement]] = Success(List
                                                  //| (WarpElement(a,Some(WarpString(a))), WarpElement(b,Some(WarpInt(1))), WarpEl
                                                  //| ement(c,Some(WarpString(a)))))
  
  val r = 1 :: "w" :: HNil                        //> r  : shapeless.::[Int,shapeless.::[String,shapeless.HNil]] = 1 :: w :: HNil
                                                  //| 
	object eval extends Poly1 {
	  implicit def caseString = at[String](_.length)
	  implicit def caseInt = at[Int](_.toString+"!")
	}
	
	r.map(eval)                               //> res4: shapeless.::[String,shapeless.::[Int,shapeless.HNil]] = 1! :: 1 :: HNi
                                                  //| l
}