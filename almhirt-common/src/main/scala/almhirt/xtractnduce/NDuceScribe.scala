/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.xtractnduce

import org.joda.time.DateTime

trait NDuceScribe {
  def setString(key: String, value: String): NDuceScript
  def setString(key: String, value: Option[String]): NDuceScript
  def setInt(key: String, value: Int): NDuceScript
  def setInt(key: String, value: Option[Int]): NDuceScript
  def setLong(key: String, value: Long): NDuceScript
  def setLong(key: String, value: Option[Long]): NDuceScript
  def setDouble(key: String, value: Double): NDuceScript
  def setDouble(key: String, value: Option[Double]): NDuceScript
  def setFloat(key: String, value: Float): NDuceScript
  def setFloat(key: String, value: Option[Float]): NDuceScript
  def setBoolean(key: String, value: Boolean): NDuceScript
  def setBoolean(key: String, value: Option[Boolean]): NDuceScript
  def setDecimal(key: String, value: BigDecimal): NDuceScript
  def setDecimal(key: String, value: Option[BigDecimal]): NDuceScript
  def setDateTime(key: String, value: DateTime): NDuceScript
  def setDateTime(key: String, value: Option[DateTime]): NDuceScript
  def setUUID(key: String, value: java.util.UUID): NDuceScript
  def setUUID(key: String, value: Option[java.util.UUID]): NDuceScript
  def setBytes(key: String, value: Array[Byte]): NDuceScript
  def setBytes(key: String, value: Option[Array[Byte]]): NDuceScript
  
  def setElement(key: String, scriptElement: NDuceScript): NDuceScript
  def setElement(key: String, scriptElement: Option[NDuceScript]): NDuceScript
  def setElements(key: String, scriptElements: NDuceScript*): NDuceScript
  def setPrimitives(key: String, primitives: Any*): NDuceScript
}

object NDuceScribe {
  def scribble(name: String): NDuceScript = NDuceAggregate(name, Seq.empty)
  def scribble(name: String, children: NDuceScriptOp*): NDuceScript = NDuceAggregate(name, children)
}