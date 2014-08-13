package almhirt

object TestConfigs {
  val logErrorConfigStr =
    """ |
        |akka {
        |    loglevel = ERROR 
        |}
        |""".stripMargin

  val logErrorConfig = com.typesafe.config.ConfigFactory.parseString(logErrorConfigStr)

  val logWarningConfigStr =
    """ |
        |akka {
        |    loglevel = WARNING
        |}
        |""".stripMargin

  val logWarningConfig = com.typesafe.config.ConfigFactory.parseString(logWarningConfigStr)

  val logDebugConfigStr =
    """ |
        |akka {
        |    loglevel = DEBUG
        |}
        |""".stripMargin

  val logDebugConfig = com.typesafe.config.ConfigFactory.parseString(logDebugConfigStr)
  
  val logInfoConfigStr =
    """ |
        |akka {
        |    loglevel = INFO
        |}
        |""".stripMargin

  val logInfoConfig = com.typesafe.config.ConfigFactory.parseString(logInfoConfigStr)
}