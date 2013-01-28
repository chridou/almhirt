package almhirt.app

object Main extends App {
  override def main(args: Array[String]) = {
    println(Splash.splash)
    (for {
      config <- MainFuns.createConfig(args)
      (almhirt, shutDown) <- MainFuns.initializeAlmhirt(null)
      shutDownHandler <- MainFuns.createShutDown(shutDown)
    } yield shutDownHandler).fold(
      prob => MainFuns.onCrash(prob),
      shutDownHandler => {
    	MainFuns.prepareShutDown(() => shutDownHandler)
      })
  }
}