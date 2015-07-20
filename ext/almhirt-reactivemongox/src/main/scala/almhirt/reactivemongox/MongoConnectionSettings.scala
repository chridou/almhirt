package almhirt.reactivemongox

import reactivemongo.api.{ MongoDriver, MongoConnection, MongoConnectionOptions }

final case class MongoConnectionSettings(nodes: List[String], numChannelsPerNode: Int) {
  def createConnection(drv: MongoDriver): MongoConnection = {
    val opts = MongoConnectionOptions(nbChannelsPerNode = numChannelsPerNode)
    drv.connection(nodes = nodes, options = opts)
  }
}