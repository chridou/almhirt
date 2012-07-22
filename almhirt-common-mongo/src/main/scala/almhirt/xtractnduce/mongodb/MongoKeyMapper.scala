package almhirt.xtractnduce.mongodb

trait MongoKeyMapper extends Function[String, String]

object MongoKeyMapper {
  def createKeyMapper(map: String => String): MongoKeyMapper =
    new MongoKeyMapper{ def apply(key: String) = map(key) }
  def createKeyMapper(idKey: String): MongoKeyMapper =
    createKeyMapper(key => if(key == idKey) "_id" else key)
  
  implicit val defaultMongoKeyMapper = createKeyMapper("id")
}
