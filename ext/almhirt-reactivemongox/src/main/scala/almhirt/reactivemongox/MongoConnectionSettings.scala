package almhirt.reactivemongox

import reactivemongo.api.{ MongoDriver, MongoConnection }

final case class MongoConnectionSettings(hosts: List[String], numChannelsPerNode: Int) {
  def createConnection(drv: MongoDriver): MongoConnection = drv.connection(hosts, nbChannelsPerNode = numChannelsPerNode)
}