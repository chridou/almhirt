package almhirt.converters

import scala.concurrent.duration._
import almhirt.common._

object FiniteDurationToStringConverter {
  val adjustingConverterInst = new FiniteDurationToStringConverter {
    def convert(dur: FiniteDuration): String = {
      val nanos = dur.toNanos
      
      if(nanos < 1000L)
        dur.timeUnitString(NANOSECONDS)
      else if(nanos < 1000L*1000L)
        dur.timeUnitString(MICROSECONDS)
      else if(nanos < 1000L*1000L*1000L)
        dur.timeUnitString(MILLISECONDS)
      else if(nanos < 60L*1000L*1000L*1000L)
        dur.timeUnitString(SECONDS)
       else if(nanos < 60L*60L*1000L*1000L*1000L)
        dur.timeUnitString(MINUTES)
       else if(nanos < 24L*60L*60L*60L*1000L*1000L*1000L)
        dur.timeUnitString(HOURS)
       else
        dur.timeUnitString(DAYS)
      
    }
  }
  
  
  val millisecondsConverterInst = new FiniteDurationToStringConverter {
    def convert(dur: FiniteDuration): String = {
      dur.millisecondsString()
    }
  }
  
  val default = adjustingConverterInst
}

trait FiniteDurationToStringConverter {
  def convert(dur: FiniteDuration): String
}
