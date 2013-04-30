package riftwarp

package object std {
  object funs extends WarpPackageFuns with PackageBuilderFuns with PackageExtractorFuns
  object warpbuilder extends WarpPackageFuns with PackageBuilderFuns with PackageBuilderOps
  object kit extends WarpPackageFuns with PackageBuilderFuns with PackageBuilderOps with PackageExtractorFuns
  
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
}