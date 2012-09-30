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
package almhirt.xtractnduce.casbah

trait MongoKeyMapper extends Function[String, String]{
  def inverse(keySubstitute: String): String
}

object MongoKeyMapper {
  def createKeyMapper(map: String => String, mapInverse: String => String): MongoKeyMapper =
    new MongoKeyMapper{ 
      def apply(key: String) = map(key)
      def inverse(potentialKey: String) = mapInverse(potentialKey)
      }
  def createKeyMapper(idKey: String): MongoKeyMapper =
    createKeyMapper(key => if(key == idKey) "_id" else key, potentialKey => if(potentialKey == "_id") idKey else potentialKey)
  
  implicit val defaultMongoKeyMapper = createKeyMapper("id")
  val identityKeyMapper = createKeyMapper(identity, identity)
}
