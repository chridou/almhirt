package almhirt.common

trait CloseHandle{
  final def apply() { close() }
  def close()
}