package riftwarp

import almhirt.common._

package object std {
  object funs extends WarpPackageFuns with PackageBuilderFuns with PackageExtractorFuns
  object warpbuilder extends WarpPackageFuns with PackageBuilderFuns with PackageBuilderOps
  object kit extends WarpPackageFuns with PackageBuilderFuns with PackageBuilderOps with PackageExtractorFuns with RiftWarpFuns

  object default {
    implicit val StdLibJsonCordDematerializer = ToJsonCordDematerializer
    implicit val StdLibJsonStringDematerializer = ToJsonStringDematerializer
    implicit val StdLibXmlElemDematerializer = ToNoisyXmlElemDematerializer
    implicit val StdLibXmlStringDematerializer = ToNoisyXmlStringDematerializer
    implicit val StdLibJsonRematerializer = FromStdLibJsonRematerializer
    implicit val StdLibJsonStringRematerializer = FromJsonStringRematerializer
    implicit val StdLibJsonCordRematerializer = FromJsonCordRematerializer
    implicit val StdLibXmlRematerializer = FromStdLibXmlRematerializer
    implicit val StdLibXmlStringRematerializer = FromXmlStringRematerializer

  }
  
  implicit val WarpPrimitiveBooleanPacker = BooleanWarpPacker
  implicit val WarpPrimitiveBooleanUnpacker = BooleanWarpUnpacker
  implicit val WarpPrimitiveStringPacker = StringWarpPacker
  implicit val WarpPrimitiveStringUnpacker = StringWarpUnpacker
  implicit val WarpPrimitiveBytePacker = ByteWarpPacker
  implicit val WarpPrimitiveByteUnpacker = ByteWarpUnpacker
  implicit val WarpPrimitiveIntPacker = IntWarpPacker
  implicit val WarpPrimitiveIntUnpacker = IntWarpUnpacker
  implicit val WarpPrimitiveLongPacker = LongWarpPacker
  implicit val WarpPrimitiveLongUnpacker = LongWarpUnpacker
  implicit val WarpPrimitiveBigIntPacker = BigIntWarpPacker
  implicit val WarpPrimitiveBigIntUnpacker = BigIntWarpUnpacker
  implicit val WarpPrimitiveFloatPacker = FloatWarpPacker
  implicit val WarpPrimitiveFloatUnpacker = FloatWarpUnpacker
  implicit val WarpPrimitiveDoublePacker = DoubleWarpPacker
  implicit val WarpPrimitiveDoubleUnpacker = DoubleWarpUnpacker
  implicit val WarpPrimitiveBigDecimalPacker = BigDecimalWarpPacker
  implicit val WarpPrimitiveBigDecimalUnpacker = BigDecimalWarpUnpacker
  implicit val WarpPrimitiveUuidPacker = UuidWarpPacker
  implicit val WarpPrimitiveUuidUnpacker = UuidWarpUnpacker
  implicit val WarpPrimitiveUriPacker = UriWarpPacker
  implicit val WarpPrimitiveUriUnpacker = UriWarpUnpacker
  implicit val WarpPrimitiveDateTimePacker = DateTimeWarpPacker
  implicit val WarpPrimitiveDateTimeUnpacker = DateTimeWarpUnpacker
  implicit val WarpPrimitiveLocalDateTimePacker = DateTimeWarpPacker
  implicit val WarpPrimitiveLocalDateTimeUnpacker = DateTimeWarpUnpacker
  implicit val WarpPrimitiveDurationPacker = DurationWarpPacker
  implicit val WarpPrimitiveDurationUnpacker = DurationWarpUnpacker
  implicit val WarpByteArrayPacker = ByteArrayWarpPacker
  implicit val WarpByteArrayUnpacker = ByteArrayWarpUnpacker

  implicit object WarpPrimitiveToBooleanConverterInst extends WarpPrimitiveToBooleanConverter
  implicit object WarpPrimitiveToStringConverterInst extends WarpPrimitiveToStringConverter
  implicit object WarpPrimitiveToByteConverterInst extends WarpPrimitiveToByteConverter
  implicit object WarpPrimitiveToIntConverterInst extends WarpPrimitiveToIntConverter
  implicit object WarpPrimitiveToLongConverterInst extends WarpPrimitiveToLongConverter
  implicit object WarpPrimitiveToBigIntConverterInst extends WarpPrimitiveToBigIntConverter
  implicit object WarpPrimitiveToFloatConverterInst extends WarpPrimitiveToFloatConverter
  implicit object WarpPrimitiveToDoubleConverterInst extends WarpPrimitiveToDoubleConverter
  implicit object WarpPrimitiveToBigDecimalConverterInst extends WarpPrimitiveToBigDecimalConverter
  implicit object WarpPrimitiveToUuidConverterConverterInst extends WarpPrimitiveToUuidConverter
  implicit object WarpPrimitiveToUriConverterInst extends WarpPrimitiveToUriConverter
  implicit object WarpPrimitiveToDateTimeConverterInst extends WarpPrimitiveToDateTimeConverter
  implicit object WarpPrimitiveToLocalDateTimeConverterInst extends WarpPrimitiveToLocalDateTimeConverter
  implicit object WarpPrimitiveToDurationConverterInst extends WarpPrimitiveToDurationConverter

  implicit class RematerializeFromOps[From](self: From) {
    def rematerialize(implicit rematerializer: Rematerializer[From]): AlmValidation[WarpPackage] =
      rematerializer.rematerialize(self)
  }

  implicit class PackagingyOps[T](self: T) {
    def pack(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpPackage] = packer.pack(self)
    def packFlat(implicit packer: WarpPacker[T]): AlmValidation[WarpPackage] = packer.pack(self)(WarpPackers.NoWarpPackers)
  }

}