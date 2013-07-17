package almhirt.testing

import com.typesafe.config.ConfigFactory

object TestConfigs {
   val default = {
     val str = 
"""
    akka {
      loglevel = "DEBUG"
      actor {
    	debug {
    	  # enable DEBUG logging of all AutoReceiveMessages (Kill, PoisonPill et.c.)
          #autoreceive = on
          #receive = on
          #lifecycle = on
        }
      }
    }
"""
     ConfigFactory.parseString(str).withFallback(ConfigFactory.load())
   }
}