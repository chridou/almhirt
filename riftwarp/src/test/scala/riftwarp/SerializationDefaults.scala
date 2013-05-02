package riftwarp

object SerializationDefaults {
  implicit val DefaultTestObjectAPacker = TestObjectAPacker
  implicit val DefaultTestObjectAUnpacker = TestObjectAUnpacker
  implicit val DefaultPrimitiveTypesPacker = PrimitiveTypesPacker
  implicit val DefaultPrimitiveTypesUnpacker = PrimitiveTypesUnpacker
  implicit val DefaultPrimitiveListMAsPacker = PrimitiveListMAsPacker
  implicit val DefaultPrimitiveListMAsUnpacker = PrimitiveListMAsUnpacker
  implicit val DefaultComplexMAsPacker = ComplexMAsPacker
  implicit val DefaultComplexMAsUnpacker = ComplexMAsUnpacker
  implicit val DefaultPrimitiveMapsPacker = PrimitiveMapsPacker
  implicit val DefaultPrimitiveMapsUnpacker = PrimitiveMapsUnpacker
  implicit val DefaultComplexMapsPacker = ComplexMapsPacker
  implicit val DefaultComplexMapsUnpacker = ComplexMapsUnpacker
  implicit val DefaultTestAddressPacker = TestAddressPacker
  implicit val DefaultTestAddressUnpacker = TestAddressUnpacker
}