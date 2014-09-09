package almhirt.httpx.spray.marshalling

object DefaultMarshallingInstances {
  implicit val BooleanMarshallingInst = BooleanMarshalling
  implicit val StringMarshallingInst = StringMarshalling
  implicit val ByteMarshallingInst = ByteMarshalling
  implicit val ShortMarshallingInst = ShortMarshalling
  implicit val IntMarshallingInst = IntMarshalling
  implicit val LongMarshallingInst = LongMarshalling
  implicit val BigIntMarshallingInst = BigIntMarshalling
  implicit val FloatMarshallingInst = FloatMarshalling
  implicit val DoubleMarshallingInst = DoubleMarshalling
  implicit val BigDecimalMarshallingInst = BigDecimalMarshalling
  implicit val UriMarshallingInst = UriMarshalling
  implicit val UuidMarshallingInst = UuidMarshalling
  implicit val LocalDateTimeMarshallingInst = LocalDateTimeMarshalling
  implicit val DateTimeMarshallingInst = DateTimeMarshalling
  implicit val DurationMarshallingInst = DurationMarshalling

  implicit val BooleansMarshallingInst = BooleansMarshalling
  implicit val StringsMarshallingInst = StringsMarshalling
  implicit val BytesMarshallingInst = BytesMarshalling
  implicit val ShortsMarshallingInst = ShortsMarshalling
  implicit val IntsMarshallingInst = IntsMarshalling
  implicit val LongsMarshallingInst = LongsMarshalling
  implicit val BigIntsMarshallingInst = BigIntsMarshalling
  implicit val FloatsMarshallingInst = FloatsMarshalling
  implicit val DoublesMarshallingInst = DoublesMarshalling
  implicit val BigDecimalsMarshallingInst = BigDecimalsMarshalling
  implicit val UrisMarshallingInst = UrisMarshalling
  implicit val UuidsMarshallingInst = UuidsMarshalling
  implicit val LocalDateTimesMarshallingInst = LocalDateTimesMarshalling
  implicit val DateTimesMarshallingInst = DateTimesMarshalling
  implicit val DurationsMarshallingInst = DurationsMarshalling

  implicit val EventMarshallingInst = EventMarshalling
  implicit val CommandMarshallingInst = CommandMarshalling
  implicit val ProblemMarshallingInst = ProblemMarshalling
  implicit val CommandResponseMarshallingInst = CommandResponseMarshalling
  
  implicit val EventsMarshallingInst = EventsMarshalling
  implicit val CommandsMarshallingInst = CommandsMarshalling
  implicit val ProblemsMarshallingInst = ProblemsMarshalling
}