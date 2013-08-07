package almhirt.common

trait CloseHandle extends Function0[Unit] { self =>
  final def apply() { close() }
  def close()
  final def andThen(other: CloseHandle): CloseHandle =
    new CloseHandle {
      def close() { self.close(); other.close() }
    }
}