package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val barracks = RiftWarpBarracks.unsafe          //> barracks  : almhirt.riftwarp.RiftWarpBarracks = almhirt.riftwarp.RiftWarpBar
                                                  //| racks$$anon$1@7b2be1bd
  barracks.addDecomposer(new TestObjectADecomposer())
  
  val testObject = new TestObjectA("Peter", Some("Paul"), 15)
                                                  //> testObject  : almhirt.riftwarp.TestObjectA = TestObjectA(Peter,Some(Paul),15
                                                  //| )
  
  val decomposer = barracks.tryGetDecomposerFor(testObject).get
                                                  //> decomposer  : almhirt.riftwarp.Decomposer[almhirt.riftwarp.TestObjectA] = al
                                                  //| mhirt.riftwarp.TestObjectADecomposer@603b1d04
  val dematerializer = new dematerializers.ToMapDematerializer(Map.empty)(barracks)
                                                  //> dematerializer  : almhirt.riftwarp.impl.dematerializers.ToMapDematerializer 
                                                  //| = almhirt.riftwarp.impl.dematerializers.ToMapDematerializer@2393385d
  val res = decomposer.decompose(testObject)(dematerializer).bind(_.dematerialize)
                                                  //> res  : scalaz.Validation[almhirt.common.Problem,almhirt.riftwarp.Demateriali
                                                  //| zer#DematerializesTo] = Success(Map(typedescriptor -> TypeDescriptor(almhirt
                                                  //| .riftwarp.TestObjectA), name -> Peter, friend -> Paul, age -> 15))
  res.forceResult                                 //> res0: almhirt.riftwarp.Dematerializer#DematerializesTo = Map(typedescriptor 
                                                  //| -> TypeDescriptor(almhirt.riftwarp.TestObjectA), name -> Peter, friend -> Pa
                                                  //| ul, age -> 15)
}