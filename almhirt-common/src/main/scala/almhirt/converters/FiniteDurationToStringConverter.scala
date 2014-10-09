package almhirt.converters

import scala.concurrent.duration._
import almhirt.common._

object FiniteDurationToStringConverter {
  val adjustingConverterInst = new FiniteDurationToStringConverter {
    def convert(dur: FiniteDuration): String = {
      val nanos = Math.abs(dur.toNanos)
      
      if(nanos < 1000L)
        dur.timeUnitString(NANOSECONDS, Some(0))
      else if(nanos < 1000L*1000L)
        dur.timeUnitString(MICROSECONDS, Some(3))
      else if(nanos < 1000L*1000L*1000L)
        dur.timeUnitString(MILLISECONDS, Some(3))
      else if(nanos < 60L*1000L*1000L*1000L)
        dur.timeUnitString(SECONDS, Some(2))
       else if(nanos < 60L*60L*1000L*1000L*1000L)
        dur.timeUnitString(MINUTES, Some(2))
       else if(nanos < 24L*60L*60L*1000L*1000L*1000L)
        dur.timeUnitString(HOURS, Some(2))
       else
        dur.timeUnitString(DAYS, Some(2))
      
    }
  }
  
  
  val millisecondsConverterInst = new FiniteDurationToStringConverter {
    def convert(dur: FiniteDuration): String = {
      dur.millisecondsString(None)
    }
  }
  
  val default = adjustingConverterInst
}

trait FiniteDurationToStringConverter {
  def convert(dur: FiniteDuration): String
}
