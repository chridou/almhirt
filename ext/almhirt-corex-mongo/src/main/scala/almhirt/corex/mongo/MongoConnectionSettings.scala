package almhirt.corex.mongo

import reactivemongo.api.{ MongoDriver, MongoConnection }

final case class MongoConnectionSettings(hosts: List[String]) {
  def createConnection(drv: MongoDriver): MongoConnection = drv.connection(hosts)
}