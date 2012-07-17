package almhirt.xtract.mongodb

import scalaz.syntax.validation._
import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._
import almhirt.xtract.XTractor
import com.mongodb.casbah.Imports._

//class MongoXtractor(mongoObj: MongoDBObject) extends XTractor {
//  type T = MongoDBObject
//  def extractString(key: String): AlmValidationSingleBadData[String]
//  def extractInt(key: String): AlmValidationSingleBadData[Int]
//  def extractLong(key: String): AlmValidationSingleBadData[Long]
//  def extractDouble(key: String): AlmValidationSingleBadData[Double]
//  def extractOptionalString(key: String): AlmValidationSingleBadData[Option[String]]
//  def extractOptionalInt(key: String): AlmValidationSingleBadData[Option[Int]]
//  def extractOptionalLong(key: String): AlmValidationSingleBadData[Option[Long]]
//  def extractOptionalDouble(key: String): AlmValidationSingleBadData[Option[Double]]
//  def firstChild(key: String): AlmValidationSingleBadData[MongoXtractor]
//  def mapOptional[U](key: String, mapXtractor: XTractor => AlmValidationSingleBadData[U]): AlmValidationSingleBadData[Option[U]]
//  def mapOptionalM[U](key: String, mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[Option[U]]
//  def flatMapOptional[U](key: String, mapXtractor: XTractor => AlmValidationSingleBadData[Option[U]]): AlmValidationSingleBadData[Option[U]]
//  def mapAll[U](key: String, mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[List[U]]
//}