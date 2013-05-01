package riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._
import scalaz._, Scalaz._
import almhirt.common.AlmValidation
import java.util.UUID
import org.joda.time.DateTime
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.DoubleWarpPacker

object TestObjectAPacker extends WarpPacker[TestObjectA] with RegisterableWarpPacker {
  val riftDescriptor = RiftDescriptor(classOf[TestObjectA])
  val alternativeRiftDescriptors = Nil
  def pack(what: TestObjectA)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    riftDescriptor ~>
      Bytes("arrayByte", what.arrayByte) ~>
      Blob("blob", what.blob) ~>
      LookUp("primitiveTypes", what.primitiveTypes) ~>
      LookUp("primitiveListMAs", what.primitiveListMAs) ~>
      LookUp("primitiveVectorMAs", what.primitiveVectorMAs) ~>
      LookUpOpt("primitiveSetMAs", what.primitiveSetMAs) ~>
      LookUp("primitiveIterableMAs", what.primitiveIterableMAs) ~>
      LookUp("complexMAs", what.complexMAs) ~>
      LookUp("primitiveMaps", what.primitiveMaps) ~>
      LookUp("complexMaps", what.complexMaps) ~>
      LookUpOpt("addressOpt", what.addressOpt) ~>
      LookUp("trees", what.trees)
  }
}

object TestObjectAUnpacker extends RegisterableWarpUnpacker[TestObjectA] {
  val riftDescriptor = RiftDescriptor(classOf[TestObjectA])
  val alternativeRiftDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers) = {
    withFastLookUp(from) { lookup =>
      for {
        arrayByte <- lookup.getBytes("arrayByte")
        blob <- lookup.getBytes("blob")
        primitiveTypes <- lookup.getObjByTag[PrimitiveTypes]("primitiveTypes")
        primitiveListMAs <- lookup.getObjByTag[PrimitiveListMAs]("primitiveListMAs")
        primitiveVectorMAs <- lookup.getObjByTag[PrimitiveVectorMAs]("primitiveVectorMAs")
        primitiveSetMAs <- lookup.tryGetObjByTag[PrimitiveSetMAs]("primitiveSetMAs")
        primitiveIterableMAs <- lookup.getObjByTag[PrimitiveIterableMAs]("primitiveIterableMAs")
        complexMAs <- lookup.getObjByTag[ComplexMAs]("complexMAs")
        primitiveMaps <- lookup.getObjByTag[PrimitiveMaps]("primitiveMaps")
        complexMaps <- lookup.getObjByTag[ComplexMaps]("complexMaps")
        addressOpt <- lookup.tryGetObjByTag[TestAddress]("addressOpt")
        trees <- lookup.getObjByTag[Trees]("trees")
      } yield TestObjectA(arrayByte, blob, primitiveTypes, primitiveListMAs, primitiveVectorMAs, primitiveSetMAs, primitiveIterableMAs, complexMAs, primitiveMaps, complexMaps, addressOpt, trees)
    }
  }
}

class PrimitiveTypesPacker extends WarpPacker[PrimitiveTypes] with RegisterableWarpPacker {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveTypes])
  val alternativeRiftDescriptors = Nil
  def pack(what: PrimitiveTypes)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    riftDescriptor ~>
      P("str", what.str) ~>
      P("bool", what.bool) ~>
      P("byte", what.byte) ~>
      P("int", what.int) ~>
      P("long", what.long) ~>
      P("bigInt", what.bigInt) ~>
      P("float", what.float) ~>
      P("double", what.double) ~>
      P("bigDec", what.bigDec) ~>
      P("dateTime", what.dateTime) ~>
      P("uuid", what.uuid)
}

class PrimitiveTypesUnpacker extends RegisterableWarpUnpacker[PrimitiveTypes] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveTypes])
  val alternativeRiftDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers) =
    withFastLookUp(from) { lookup =>
      for {
        str <- lookup.getAs[String]("str")
        bool <- lookup.getAs[Boolean]("bool")
        byte <- lookup.getAs[Byte]("byte")
        int <- lookup.getAs[Int]("int")
        long <- lookup.getAs[Long]("long")
        bigInt <- lookup.getAs[BigInt]("bigInt")
        float <- lookup.getAs[Float]("float")
        double <- lookup.getAs[Double]("double")
        bigDec <- lookup.getAs[BigDecimal]("bigDec")
        dateTime <- lookup.getAs[DateTime]("dateTime")
        uuid <- lookup.getAs[UUID]("uuid")
      } yield PrimitiveTypes(str, bool, byte, int, long, bigInt, float, double, bigDec, dateTime, uuid)
    }
}

class PrimitiveListMAsPacker extends WarpPacker[PrimitiveListMAs] with RegisterableWarpPacker {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveListMAs])
  val alternativeRiftDescriptors = Nil
  def pack(what: PrimitiveListMAs)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    riftDescriptor ~>
      CP("listString", what.listString) ~>
      CLookUp("listInt", what.listInt) ~>
      CWith("listDouble", what.listDouble, DoubleWarpPacker) ~>
      CLookUp("listBigDecimal", what.listBigDecimal) ~>
      CLookUp("listDateTime", what.listDateTime)
  }
}

class PrimitiveListMAsRecomposer extends RegisterableWarpUnpacker[PrimitiveListMAs] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveListMAs])
  val alternativeRiftDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers) = {
    withFastLookUp(from) { lookup =>
      for {
        listString <- lookup.getPrimitives[String]("listString").map(_.toList)
        listInt <- lookup.getPrimitives[Int]("listInt").map(_.toList)
        listDouble <- lookup.getPrimitives[Double]("listDouble").map(_.toList)
        listBigDecimal <- lookup.getPrimitives[BigDecimal]("listBigDecimal").map(_.toList)
        listDateTime <- lookup.getPrimitives[DateTime]("listDateTime").map(_.toList)
      } yield PrimitiveListMAs(listString, listInt, listDouble, listBigDecimal, listDateTime)
    }
  }
}

class ComplexMAsDecomposer extends Decomposer[ComplexMAs] {
  val riftDescriptor = RiftDescriptor(classOf[ComplexMAs])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: ComplexMAs, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addIterableAllWith("addresses1", what.addresses1, new TestAddressDecomposer())
      .flatMap(_.addIterableStrict("addresses2", what.addresses2, None))
      .flatMap(_.addIterableOfComplex("addresses3", what.addresses3, None))
      .flatMap(_.addIterable("anything", what.anything, None))
  }
}

class ComplexMAsRecomposer extends Recomposer[ComplexMAs] {
  val riftDescriptor = RiftDescriptor(classOf[ComplexMAs])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[ComplexMAs] = {
    val addresses1 = from.getManyComplexByTag[List, TestAddress]("addresses1", None).toAgg
    val addresses2 = from.getManyComplexByTag[Vector, TestAddress]("addresses2", None).toAgg
    val addresses3 = from.getManyComplexByTag[Set, TestAddress]("addresses3", None).toAgg
    val anything = from.getMany[List]("anything", None).toAgg
    (addresses1
      |@| addresses2
      |@| addresses3
      |@| anything)(ComplexMAs(_, _, _, _))
  }
}

class PrimitiveMapsDecomposer extends Decomposer[PrimitiveMaps] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveMaps])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PrimitiveMaps, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addMapOfPrimitives("mapIntInt", what.mapIntInt)
      .flatMap(_.addMapOfPrimitives("mapStringInt", what.mapStringInt))
      .flatMap(_.addMapOfPrimitives("mapUuidDateTime", what.mapUuidDateTime))
  }
}

class PrimitiveMapsRecomposer extends Recomposer[PrimitiveMaps] {
  val riftDescriptor = RiftDescriptor(classOf[PrimitiveMaps])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[PrimitiveMaps] = {
    val mapIntInt = from.getMapOfPrimitives[Int, Int]("mapIntInt").toAgg
    val mapStringInt = from.getMapOfPrimitives[String, Int]("mapStringInt").toAgg
    val mapUuidDateTime = from.getMapOfPrimitives[UUID, DateTime]("mapUuidDateTime").toAgg
    (mapIntInt |@| mapStringInt |@| mapUuidDateTime)(PrimitiveMaps.apply)
  }
}

class ComplexMapsDecomposer extends Decomposer[ComplexMaps] {
  val riftDescriptor = RiftDescriptor(classOf[ComplexMaps])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: ComplexMaps, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addMapOfComplex("mapIntTestAddress1", what.mapIntTestAddress1, None)
      .flatMap(_.addMap("mapIntAny", what.mapIntAny, None))
      .flatMap(_.addMapLiberate("mapStringAnyWithUnknown", what.mapStringAnyWithUnknown, None))
  }
}

class ComplexMapsRecomposer extends Recomposer[ComplexMaps] {
  val riftDescriptor = RiftDescriptor(classOf[ComplexMaps])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[ComplexMaps] = {
    val mapIntTestAddress1 = from.getMapComplexByTag[Int, TestAddress]("mapIntTestAddress1", None).toAgg
    val mapIntAny = from.getMapComplexByTag[Int, AnyRef]("mapIntAny", None).toAgg
    val mapStringAnyWithUnknown = from.getMap[String]("mapStringAnyWithUnknown", None).toAgg
    (mapIntTestAddress1 |@| mapIntAny |@| mapStringAnyWithUnknown)(ComplexMaps.apply)
  }
}

class TestAddressDecomposer extends Decomposer[TestAddress] {
  val riftDescriptor = RiftDescriptor(classOf[TestAddress])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: TestAddress, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addString("city", what.city)
      .addString("street", what.street).ok
  }
}

class TestAddressRecomposer extends Recomposer[TestAddress] {
  val riftDescriptor = RiftDescriptor(classOf[TestAddress])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[TestAddress] = {
    val city = from.getString("city").toAgg
    val street = from.getString("street").toAgg
    (city |@| street)(TestAddress.apply)
  }
}

class TreesDecomposer extends Decomposer[Trees] {
  val riftDescriptor = RiftDescriptor(classOf[Trees])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: Trees, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addTreeOfPrimitives("intTree", what.intTree).flatMap(
        _.addTreeOfComplex("addressTree", what.addressTree, None))
  }
}

class TreesRecomposer extends Recomposer[Trees] {
  val riftDescriptor = RiftDescriptor(classOf[Trees])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[Trees] = {
    val intTree = from.getTreeOfPrimitives[Int]("intTree").toAgg
    val addressTree = from.getTreeOfComplexByTag[TestAddress]("addressTree", None).toAgg
    (intTree |@| addressTree)(Trees.apply)
  }
}