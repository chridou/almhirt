package almhirt.testing

import com.typesafe.config.ConfigFactory

object TestConfigs {
   val default = {
     val str = 
"""
    akka {
      #loglevel = "DEBUG"
      actor {
    	debug {
    	  # enable DEBUG logging of all AutoReceiveMessages (Kill, PoisonPill et.c.)
          #receive = on
          #autoreceive = on
          #lifecycle = on
        }
      }
    }
"""
     ConfigFactory.parseString(str).withFallback(ConfigFactory.load())
   }
}