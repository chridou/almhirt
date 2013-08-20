package riftwarp.templating

object handson {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  case class AA(a: String, b: Int)
  val x = StringPart[AA]("a", _.a) combine IntPart[AA]("b", _.b)
                                                  //> x  : riftwarp.templating.Template[shapeless.::[riftwarp.templating.StringPar
                                                  //| t[riftwarp.templating.handson.AA],shapeless.::[riftwarp.templating.IntPart[r
                                                  //| iftwarp.templating.handson.AA]{val label: String; val getValue: riftwarp.tem
                                                  //| plating.handson.AA => Int},shapeless.HNil]],riftwarp.templating.handson.AA] 
                                                  //| = riftwarp.templating.TemplatePart$$anon$3@51b1373d
        
        
            
  val a = AA("a", 1)                              //> a  : riftwarp.templating.handson.AA = AA(a,1)
  x.hlist.head.getValue(a)                        //> res0: String = a
  x.hlist.tail.head.getValue(a)                   //> res1: Int = 1

}