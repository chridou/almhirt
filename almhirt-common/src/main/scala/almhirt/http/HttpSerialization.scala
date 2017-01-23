package almhirt.http

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.serialization._

trait SerializesToHttp[-TIn] extends Serializes[TIn, AlmHttpBody] {
  def serialize(what: TIn)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[AlmHttpBody]
}

trait DeserializesFromHttp[+TOut] extends Deserializes[AlmHttpBody, TOut]{
  def deserialize(what: AlmHttpBody)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[TOut]
}

trait HttpSerializer[T] {
  def serialize(what: T, mediaType: AlmMediaType)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[AlmHttpBody]

  final def serializeToDefault(what: T)(implicit params: SerializationParams = SerializationParams.empty, mp: AlmMediaTypesProvider[T]): AlmValidation[(AlmHttpBody, AlmMediaType)] =
    mp.defaultForSerialization match {
      case Some(mt) ⇒ serialize(what, mt).map((_, mt))
      case None ⇒ NoSuchElementProblem("No default media type defined").failure
    }

  final def serializeIfSupported(what: T, mediaType: AlmMediaType)(implicit params: SerializationParams = SerializationParams.empty, mp: AlmMediaTypesProvider[T]): AlmValidation[AlmHttpBody] =
    if (mp.targetMediaTypes.contains(mediaType)) {
      serialize(what, mediaType)
    } else {
      NoSuchElementProblem(s"""Media type "${mediaType.value}" is not supported for Http serialization.""").failure
    }

  final def serializer(mediaType: AlmMediaType): SerializesToHttp[T] =
    new SerializesToHttp[T] {
      override def serialize(what: T)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[AlmHttpBody] =
        HttpSerializer.this.serialize(what, mediaType)
    }
}

trait HttpDeserializer[T] {
  def deserialize(mediaType: AlmMediaType, what: AlmHttpBody)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[T]


  final def deserializer(mediaType: AlmMediaType): DeserializesFromHttp[T] =
    new DeserializesFromHttp[T] {
      def deserialize(what: AlmHttpBody)(implicit params: SerializationParams = SerializationParams.empty): AlmValidation[T] =
        HttpDeserializer.this.deserialize(mediaType, what)
    }
}