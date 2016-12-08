package almhirt.reactivemongox

import reactivemongo.api.MongoConnection
import reactivemongo.api.MongoConnectionOptions
import reactivemongo.api.MongoDriver
import reactivemongo.api.ReadPreference

final case class MongoConnectionSettings(nodes: List[String], options: MongoConnectionSettings.MongoConnectionOptions)

object MongoConnectionSettings {
  final case class MongoConnectionOptions(numChannelsPerNode: Int, sslEnabled: Boolean, sslAllowsInvalidCert: Boolean)

  object MongoConnectionOptions {
    implicit class MongoConnectionOptionsOps(val self: MongoConnectionOptions) extends AnyVal {
      def toOptions: reactivemongo.api.MongoConnectionOptions =
        reactivemongo.api.MongoConnectionOptions(nbChannelsPerNode = self.numChannelsPerNode, sslEnabled = self.sslEnabled, sslAllowsInvalidCert = self.sslAllowsInvalidCert)
        
      def toOptions(readPreference: ReadPreference): reactivemongo.api.MongoConnectionOptions =
        reactivemongo.api.MongoConnectionOptions(nbChannelsPerNode = self.numChannelsPerNode, sslEnabled = self.sslEnabled, sslAllowsInvalidCert = self.sslAllowsInvalidCert, readPreference = readPreference)  
    }
  }

  implicit class MongoConnectionSettingsOps(val self: MongoConnectionSettings) extends AnyVal {
    def createConnection(drv: MongoDriver): MongoConnection = {
      drv.connection(nodes = self.nodes, options = self.options.toOptions)
    }
    
    def createConnection(drv: MongoDriver, readPreference: ReadPreference): MongoConnection = {
      drv.connection(nodes = self.nodes, options = self.options.toOptions(readPreference))
    }
  }
}