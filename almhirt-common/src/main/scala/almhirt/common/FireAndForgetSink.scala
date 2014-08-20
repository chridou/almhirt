package almhirt.common

trait FireAndForgetSink[TElement] extends Function1[TElement, Unit] {
  final def apply(element: TElement) { accept(element) }
  def accept(element: TElement)
}

object FireAndForgetSink {
  def devNull[TElement]: FireAndForgetSink[TElement] =
    new FireAndForgetSink[TElement] { def accept(element: TElement) {} }

  def delegating[TElement](onElement: TElement => Unit): FireAndForgetSink[TElement] =
    new FireAndForgetSink[TElement] { def accept(element: TElement) { onElement(element) } }
}