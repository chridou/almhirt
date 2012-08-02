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
