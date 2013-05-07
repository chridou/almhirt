package almhirt.http

import almhirt.common._

object RequestResponseWorkflowBuilder {
  import HttpBuildingBlox._
  def terminal[TFrom, T, U, TTo](f: T => AlmValidation[U], successCode: HttpStatusCode)(implicit context: HttpRequestInstances[TFrom], instances: HttpInstances, unmarshaller: HttpUnmarshaller[T], marshaller: HttpMarshaller[U], consumer: HttpResponseConsumer[TTo]): (TFrom, TTo) => Unit = {
    (source: TFrom, dest: TTo) =>
    for {
      req <- extractRequest(source)
      unmarshalled <- unmarshal(req)
      res <- f(unmarshalled)
      marshalled <- marshaller(res, "")
    }
  }
}