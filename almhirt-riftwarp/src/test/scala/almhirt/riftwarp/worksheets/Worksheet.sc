package almhirt.riftwarp.worksheets

import almhirt.syntax.almvalidation._
import almhirt.riftwarp._
import almhirt.riftwarp.impl._

object Worksheet {
  val barracks = RiftWarpBarracks.unsafe
  barracks.addDecomposer(new TestObjectADecomposer())
  barracks.addRecomposer(new TestObjectARecomposer())
  
  val testObject = new TestObjectA("Peter", Some("Paul"), 15)
  val decomposer = barracks.tryGetDecomposerFor(testObject).get
  val dematerializer = new dematerializers.ToMapDematerializer(Map.empty)(barracks)
  val resV = RiftWarpFuns.prepareForWarp(testObject)(decomposer, dematerializer)
  val res = resV.forceResult
//  val rematerializationArray = rematerializers.FromMapRematerializationArray.createRematerializationArray(res)

}