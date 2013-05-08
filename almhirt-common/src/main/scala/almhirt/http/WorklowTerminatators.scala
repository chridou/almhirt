package almhirt.http

import almhirt.common._

object WorklowTerminatators {
  def finishTerminal[TFrom, T, U, TTo](toResponse: (TFrom) => HttpResponse)(implicit respConsumer: ResponseConsumer[TTo]): (TFrom, TTo) => Unit =
    (source: TFrom, dest: TTo) =>
      respConsumer(dest, toResponse(source))

  def finishWithResponseResult[TFrom, T, U, TRes](toResponse: (TFrom) => HttpResponse)(implicit gen: SpecialResponseGenerator[TRes]): (TFrom) => AlmValidation[TRes] =
    (source: TFrom) =>
      gen(toResponse(source))

  def finishTerminalF[TFrom, T, U, TTo](toResponse: (TFrom) => AlmFuture[HttpResponse])(implicit respConsumer: ResponseConsumer[TTo], problemConsumer: Consumer[Problem], hec: HasExecutionContext): (TFrom, TTo) => Unit =
    (source: TFrom, dest: TTo) =>
      toResponse(source).onComplete(
        fail => problemConsumer(fail),
        succ => respConsumer(dest, succ))

  def finishWithResponseResultF[TFrom, T, U, TRes](toResponse: (TFrom) => AlmFuture[HttpResponse])(implicit gen: SpecialResponseGenerator[TRes], hec: HasExecutionContext): (TFrom) => AlmFuture[TRes] =
    (source: TFrom) =>
      toResponse(source).mapV(rsp => gen(rsp))
      
}