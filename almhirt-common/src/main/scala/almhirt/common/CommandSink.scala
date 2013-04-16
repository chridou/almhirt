package almhirt.common

trait CommandSink {
  def comsume(command: Command)
}