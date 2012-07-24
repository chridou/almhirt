package almhirt.xtractnduce.xml

import almhirt.xtractnduce.NDuceScript

trait ScribbableXmlXTractor { self: XmlXTractor =>
  def scribble(): NDuceScript = {
    
    sys.error("")
  }
}