package almhirt.xtractnduce.xml

import almhirt.xtractnduce.NDuceScribe._

object XTractNDuceSamples {
  val bob = 
    <Bob>
	  <id>0</id>
	  <name>Bob</name>
	  <age>33</age>
	  <dps>1.37</dps>
	  <ageAsText>33</ageAsText>
	  <spaces>  </spaces>
	  <address>
		<containerTypeName>
	      <street>Downing Street</street>
	      <city>London</city>
 		</containerTypeName>
     </address>
	  <scores>
        <value>1</value>
        <value>2</value>
        <value>3</value>
        <value>4</value>
        <value>5</value>
        <value>6</value>
        <value>7</value>
        <value>8</value>
        <value>9</value>
        <value>10</value>
	  </scores>
	  <gameTimes>
          <containerTypeName><aoe>12.3</aoe></containerTypeName>
          <anyNameYouWant><eve>29.1</eve></anyNameYouWant>
          <value><pacman>1229.1</pacman></value>
	  </gameTimes>
    </Bob>
    
  val bobScript = 
    scribble("Bob")
      .setLong("id", 0L)
      .setString("name", "Bob")
      .setInt("age", 33)
      .setDouble("dps", 1.37)
      .setString("ageAsText", "33")
      .setString("spaces", "  ")
      .setElement("address", 
          scribble("containerTypeName")
            .setString("street", "Downing Street")
            .setString("city", "London"))
      .setPrimitives("scores", (1 to 10): _*)
      .setElements("gameTimes",
          scribble("containerTypeName").setDouble("aoe", 12.3),
          scribble("anyNameYouWant").setDouble("eve", 29.1),
          scribble("value").setDouble("pacman", 1229.1))
}
