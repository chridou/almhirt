package almhirt.reactivemongox

import reactivemongo.api.{ MongoDriver, MongoConnection, MongoConnectionOptions }

final case class MongoConnectionSettings(nodes: List[String], numChannelsPerNode: Int)

object MongoConnectionSettings {
  implicit class MongoConnectionSettingsOps(val self: MongoConnectionSettings) extends AnyVal {
    def createConnection(drv: MongoDriver): MongoConnection = {
      val opts = MongoConnectionOptions(nbChannelsPerNode = self.numChannelsPerNode)
      drv.connection(nodes = self.nodes, options = opts)
    }
  }
}