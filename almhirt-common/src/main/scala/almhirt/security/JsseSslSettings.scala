package almhirt.security

final case class JsseSslSettings(
  // Setting this might be a security risk....
  keyStore: String,
  keyStorePassword: Option[String],
  trustStore: Option[String],
  trustStorePassword: Option[String])

object JsseSslSettings {
  private def launderPath(p: String): String = p.replace("~", System.getProperty("user.home"))

  implicit class JSSESettingsOps(val self: JsseSslSettings) extends AnyVal {
    def properties(): java.util.Properties = {
      val props = new java.util.Properties()
      props.setProperty("javax.net.ssl.keyStore", launderPath(self.keyStore))
      self.keyStorePassword.foreach(v ⇒ props.setProperty("javax.net.ssl.keyStorePassword", v))
      self.trustStore.foreach(v ⇒ props.setProperty("javax.net.ssl.trustStore", launderPath(v)))
      self.trustStorePassword.foreach(v ⇒ props.setProperty("javax.net.ssl.trustStorePassword", launderPath(v)))
      props
    }

    def setSystemProperties(): Unit = {
      val systemProps = System.getProperties
      systemProps.put("javax.net.ssl.keyStore", launderPath(self.keyStore))
      self.keyStorePassword.foreach(v ⇒ systemProps.put("javax.net.ssl.keyStorePassword", v))
      self.trustStore.foreach(v ⇒ systemProps.put("javax.net.ssl.trustStore", launderPath(v)))
      self.trustStorePassword.foreach(v ⇒ systemProps.put("javax.net.ssl.trustStorePassword", launderPath(v)))
      System.setProperties(systemProps)
    }
  }
}