package riftwarp

import java.util.UUID
import org.joda.time.{DateTime, LocalDateTime}
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std._
import riftwarp.std.kit._

object TestObjectAPacker extends WarpPacker[TestObjectA] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[TestObjectA])
  val alternativeWarpDescriptors = Nil
  def pack(what: TestObjectA)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    warpDescriptor ~>
      Bytes("bytes", what.bytes) ~>
      Blob("blob", what.blob) ~>
      LookUp("primitiveTypes", what.primitiveTypes) ~>
      LookUp("primitiveListMAs", what.primitiveListMAs) ~>
      LookUp("complexMAs", what.complexMAs) ~>
      LookUp("primitiveMaps", what.primitiveMaps) ~>
      LookUp("complexMaps", what.complexMaps) ~>
      LookUpOpt("addressOpt", what.addressOpt) ~>
      LookUp("trees", what.trees)
  }
}

object TestObjectAUnpacker extends RegisterableWarpUnpacker[TestObjectA] {
  val warpDescriptor = WarpDescriptor(classOf[TestObjectA])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers) = {
    withFastLookUp(from) { lookup =>
      for {
        bytes <- lookup.getBytes("bytes")
        blob <- lookup.getBytes("blob")
        primitiveTypes <- lookup.getTyped[PrimitiveTypes]("primitiveTypes")
        primitiveListMAs <- lookup.getTyped[PrimitiveListMAs]("primitiveListMAs")
        complexMAs <- lookup.getTyped[ComplexMAs]("complexMAs")
        primitiveMaps <- lookup.getTyped[PrimitiveMaps]("primitiveMaps")
        complexMaps <- lookup.getTyped[ComplexMaps]("complexMaps")
        addressOpt <- lookup.tryGetTyped[TestAddress]("addressOpt")
        trees <- lookup.getTyped[Trees]("trees")
      } yield TestObjectA(bytes.toVector, blob.toVector, primitiveTypes, primitiveListMAs, complexMAs, primitiveMaps, complexMaps, addressOpt, trees)
    }
  }
}

object PrimitiveTypesPacker extends WarpPacker[PrimitiveTypes] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[PrimitiveTypes])
  val alternativeWarpDescriptors = Nil
  def pack(what: PrimitiveTypes)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    warpDescriptor ~>
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
      P("localDateTime", what.localDateTime) ~>
      P("uuid", what.uuid)
}

object PrimitiveTypesUnpacker extends RegisterableWarpUnpacker[PrimitiveTypes] {
  val warpDescriptor = WarpDescriptor(classOf[PrimitiveTypes])
  val alternativeWarpDescriptors = Nil
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
        localDateTime <- lookup.getAs[LocalDateTime]("localDateTime")
        uuid <- lookup.getAs[UUID]("uuid")
      } yield PrimitiveTypes(str, bool, byte, int, long, bigInt, float, double, bigDec, dateTime, localDateTime, uuid)
    }
}

object PrimitiveListMAsPacker extends WarpPacker[PrimitiveListMAs] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[PrimitiveListMAs])
  val alternativeWarpDescriptors = Nil
  def pack(what: PrimitiveListMAs)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    warpDescriptor ~>
      CP("listString", what.listString) ~>
      CLookUp("listInt", what.listInt) ~>
      CWith("listDouble", what.listDouble, DoubleWarpPacker) ~>
      CLookUp("listBigDecimal", what.listBigDecimal) ~>
      CLookUp("listDateTime", what.listDateTime)
  }
}

object PrimitiveListMAsUnpacker extends RegisterableWarpUnpacker[PrimitiveListMAs] {
  val warpDescriptor = WarpDescriptor(classOf[PrimitiveListMAs])
  val alternativeWarpDescriptors = Nil
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

object ComplexMAsPacker extends WarpPacker[ComplexMAs] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[ComplexMAs])
  val alternativeWarpDescriptors = Nil
  def pack(what: ComplexMAs)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    warpDescriptor ~>
      CWith("addresses1", what.addresses1, TestAddressPacker) ~>
      CLookUp("addresses2", what.addresses2) ~>
      CLookUp("addresses3", what.addresses3) ~>
      CLookUp("anything", what.anything)
  }
}

object ComplexMAsUnpacker extends RegisterableWarpUnpacker[ComplexMAs] {
  val warpDescriptor = WarpDescriptor(classOf[ComplexMAs])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers) = {
    withFastLookUp(from) { lookup =>
      for {
        addresses1 <- lookup.getManyWith("addresses1", TestAddressUnpacker).map(_.toList)
        addresses2 <- lookup.getManyTyped[TestAddress]("addresses2")
        addresses3 <- lookup.getManyTyped[TestAddress]("addresses3").map(_.toSet)
        anything <- lookup.getMany("anything").map(_.toList)
      } yield ComplexMAs(addresses1, addresses2, addresses3, anything)
    }
  }
}

object PrimitiveMapsPacker extends WarpPacker[PrimitiveMaps] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[PrimitiveMaps])
  val alternativeWarpDescriptors = Nil
  def pack(what: PrimitiveMaps)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    warpDescriptor ~>
      MP("mapIntInt", what.mapIntInt) ~>
      MP("mapStringInt", what.mapStringInt) ~>
      MP("mapUuidDateTime", what.mapUuidDateTime)
  }
}

object PrimitiveMapsUnpacker extends RegisterableWarpUnpacker[PrimitiveMaps] {
  val warpDescriptor = WarpDescriptor(classOf[PrimitiveMaps])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers) = {
    withFastLookUp(from) { lookup =>
      for {
        mapIntInt <- lookup.getPrimitiveAssocs[Int, Int]("mapIntInt").map(_.toMap)
        mapStringInt <- lookup.getPrimitiveAssocs[String, Int]("mapStringInt").map(_.toMap)
        mapUuidDateTime <- lookup.getPrimitiveAssocs[UUID, DateTime]("mapUuidDateTime").map(_.toMap)
      } yield PrimitiveMaps(mapIntInt, mapStringInt, mapUuidDateTime)
    }
  }
}

object ComplexMapsPacker extends WarpPacker[ComplexMaps] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[ComplexMaps])
  val alternativeWarpDescriptors = Nil
  def pack(what: ComplexMaps)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    warpDescriptor ~>
      MWith("mapIntTestAddress1", what.mapIntTestAddress1, TestAddressPacker) ~>
      MLookUp("mapIntAny", what.mapIntAny)
  }
}

object ComplexMapsUnpacker extends RegisterableWarpUnpacker[ComplexMaps] {
  val warpDescriptor = WarpDescriptor(classOf[ComplexMaps])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers) = {
    withFastLookUp(from) { lookup =>
      for {
        mapIntTestAddress1 <- lookup.getAssocsWith[Int, TestAddress]("mapIntTestAddress1", TestAddressUnpacker).map(_.toMap)
        mapIntAny <- lookup.getAssocsTyped[Int, AnyRef]("mapIntAny").map(_.toMap)
      } yield ComplexMaps(mapIntTestAddress1, mapIntAny)
    }
  }
}

object TestAddressPacker extends WarpPacker[TestAddress] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[TestAddress])
  val alternativeWarpDescriptors = Nil
  override def pack(what: TestAddress)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    warpDescriptor ~>
      P("city", what.city) ~>
      P("street", what.street)
  }
}

object TestAddressUnpacker extends RegisterableWarpUnpacker[TestAddress] {
  val warpDescriptor = WarpDescriptor(classOf[TestAddress])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TestAddress] = {
    withFastLookUp(from) { lookup =>
      for {
        city <- lookup.getAs[String]("city")
        street <- lookup.getAs[String]("street")
      } yield TestAddress(city, street)
    }
  }
}

object TreesPacker extends WarpPacker[Trees] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[Trees])
  val alternativeWarpDescriptors = Nil
  def pack(what: Trees)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    warpDescriptor ~>
      TP("intTree", what.intTree) ~>
      TLookUp("addressTree", what.addressTree)
  }
}

object TreesUnpacker extends RegisterableWarpUnpacker[Trees] {
  val warpDescriptor = WarpDescriptor(classOf[Trees])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Trees] = {
    withFastLookUp(from) { lookup =>
      for {
        intTree <- lookup.getPrimitivesTree[Int]("intTree")
        addressTree <- lookup.getTreeTyped[TestAddress]("addressTree")
      } yield Trees(intTree, addressTree)
    }
  }
}

object Serialization {
  def addPackers(to: WarpPackers): WarpPackers = {
    to.addTyped(TestObjectAPacker)
    to.addTyped(PrimitiveTypesPacker)
    to.addTyped(PrimitiveListMAsPacker)
    to.addTyped(ComplexMAsPacker)
    to.addTyped(PrimitiveMapsPacker)
    to.addTyped(ComplexMapsPacker)
    to.addTyped(TestAddressPacker)
    to.addTyped(TreesPacker)
    to
  }
  
  def addUnpackers(to: WarpUnpackers): WarpUnpackers = {
    to.addTyped(TestObjectAUnpacker)
    to.addTyped(PrimitiveTypesUnpacker)
    to.addTyped(PrimitiveListMAsUnpacker)
    to.addTyped(ComplexMAsUnpacker)
    to.addTyped(PrimitiveMapsUnpacker)
    to.addTyped(ComplexMapsUnpacker)
    to.addTyped(TestAddressUnpacker)
    to.addTyped(TreesUnpacker)
    to
  }
}
