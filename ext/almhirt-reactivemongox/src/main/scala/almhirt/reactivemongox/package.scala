package almhirt

import scala.language.implicitConversions
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scalaz.Validation.FlatMap._
import almhirt.common._
import reactivemongo.api.commands._
import reactivemongo.api._
import reactivemongo.bson.{ BSONDocument, BSONDocumentReader }
import reactivemongo.play.iteratees._
import play.api.libs.iteratee.{ Enumerator, Enumeratee }
import almhirt.configuration._
import com.typesafe.config.Config

package object reactivemongox {
  implicit def almWc2RmWc(self: WriteConcernAlm): WriteConcern = WriteConcernAlm.toReactiveMongoWriteConcern(self)
  implicit def almRp2RmRp(self: ReadPreferenceAlm): ReadPreference = ReadPreferenceAlm.toReactiveMongoReadPreference(self)

  implicit class BsonCollectionOps(val self: reactivemongo.api.collections.bson.BSONCollection) extends AnyVal {
    def traverseWith[T: BSONDocumentReader](selector: Option[BSONDocument], projection: Option[BSONDocument], sort: Option[BSONDocument], traverse: TraverseWindow, readPreference: ReadPreferenceAlm, stopOnError: Boolean)(implicit ctx: ExecutionContext): Enumerator[T] = {
      val effSel = selector getOrElse BSONDocument.empty
      val effProj = projection getOrElse BSONDocument.empty
      val effSort = sort getOrElse BSONDocument.empty
      val onError = if(stopOnError == true) Cursor.FailOnError[Unit]() else Cursor.Ignore();
      traverse match {
        case TraverseWindow(TraverseWindow.SkipNone, TraverseWindow.TakeAll) ⇒
          self.find(effSel, effProj).sort(effSort).cursor[T](readPreference = readPreference).enumerator(err = onError)
        case TraverseWindow(TraverseWindow.SkipNone, TraverseWindow.Take(take: Int)) ⇒
          self.find(effSel, effProj).sort(effSort).cursor[T](readPreference = readPreference).enumerator(maxDocs = take, err = onError) &> Enumeratee.take(take)
        case TraverseWindow(TraverseWindow.Skip(skip: Int), TraverseWindow.TakeAll) ⇒
          self.find(effSel, effProj).options(new QueryOpts(skipN = skip)).sort(effSort).cursor[T](readPreference = readPreference).enumerator(err = onError)
        case TraverseWindow(TraverseWindow.Skip(skip: Int), TraverseWindow.Take(take: Int)) ⇒
          self.find(effSel, effProj).options(new QueryOpts(skipN = skip)).sort(effSort).cursor[T](readPreference = readPreference).enumerator(maxDocs = take, err = onError) &> Enumeratee.take(take)
      }
    }

    def traverseWith[T: BSONDocumentReader](selector: BSONDocument, projection: BSONDocument, sort: BSONDocument, traverse: TraverseWindow, readPreference: ReadPreferenceAlm)(implicit ctx: ExecutionContext): Enumerator[T] =
      traverseWith(Some(selector), Some(projection), Some(sort), traverse, readPreference, true)

    def traverseWith[T: BSONDocumentReader](selector: BSONDocument, projection: BSONDocument, traverse: TraverseWindow, readPreference: ReadPreferenceAlm)(implicit ctx: ExecutionContext): Enumerator[T] =
      traverseWith(Some(selector), Some(projection), None, traverse, readPreference, true)

    def traverseWith[T: BSONDocumentReader](selector: BSONDocument, traverse: TraverseWindow, readPreference: ReadPreferenceAlm)(implicit ctx: ExecutionContext): Enumerator[T] =
      traverseWith(Some(selector), None, None, traverse, readPreference, true)

    def traverseWith[T: BSONDocumentReader](traverse: TraverseWindow, readPreference: ReadPreferenceAlm)(implicit ctx: ExecutionContext): Enumerator[T] =
      traverseWith(None, None, None, traverse, readPreference, true)

    def queryAlm(): QueryBuilderAlm = QueryBuilderAlm(self)

    def queryAlm(readPreference: ReadPreferenceAlm): QueryBuilderAlm = QueryBuilderAlm(self, readPreference)

    def queryAlm(selector: BSONDocument, readPreference: ReadPreferenceAlm): QueryBuilderAlm = QueryBuilderAlm(self, selector = selector, readPreference = readPreference)

    def queryAlm(selector: BSONDocument, projection: BSONDocument, readPreference: ReadPreferenceAlm): QueryBuilderAlm = QueryBuilderAlm(self, selector = selector, projection = projection, readPreference = readPreference)

    def queryAlm(selector: BSONDocument, projection: BSONDocument, sort: BSONDocument, readPreference: ReadPreferenceAlm): QueryBuilderAlm = QueryBuilderAlm(self, selector = selector, projection = projection, sort = sort, readPreference = readPreference)
  
    //def collStats: AlmFuture[BoxedAnyVal[CollStatsResult]] = self.runValueCommand(CollStats()).toAlmFuture
  }

  implicit object MongoConnectionSettingsConfigExtractor extends ConfigExtractor[MongoConnectionSettings] {
    def getValue(config: Config, path: String): AlmValidation[MongoConnectionSettings] =
      for {
        section ← config.v[Config](path)
        hosts ← section.v[List[String]]("hosts")
        numChannelsPerNode ← section.magicDefault[Int]("default", 10)("num-channels-per-node")
        sslEnabled ← section.opt[Boolean]("ssl-enabled")
        sslAllowsInvalidCert ← section.opt[Boolean]("ssl-allows-invalid-cert")
      } yield MongoConnectionSettings(hosts, options = MongoConnectionSettings.MongoConnectionOptions(
        numChannelsPerNode = numChannelsPerNode,
        sslEnabled = sslEnabled getOrElse false,
        sslAllowsInvalidCert = sslAllowsInvalidCert getOrElse false))

    def tryGetValue(config: Config, path: String): AlmValidation[Option[MongoConnectionSettings]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit val WriteConcernAlmConfigExtractorInst = new ConfigExtractor[WriteConcernAlm] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[WriteConcernAlm] =
      (config.v[com.typesafe.config.Config](path).flatMap { WriteConcernAlm.fromConfig }).leftMap(p ⇒ ConfigurationProblem(s"""Failed to configure WriteConcernAlm @$path.""", cause = Some(p)))
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[WriteConcernAlm]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit val ReadPreferenceAlmConfigExtractorInst = new ConfigExtractor[ReadPreferenceAlm] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[ReadPreferenceAlm] =
      (config.v[com.typesafe.config.Config](path).flatMap { ReadPreferenceAlm.fromConfig }).leftMap(p ⇒ ConfigurationProblem(s"""Failed to configure ReadPreferenceAlm @$path.""", cause = Some(p)))
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[ReadPreferenceAlm]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit val ReadWriteModeSupportsReadingConfigExtractorInst = new ConfigExtractor[ReadWriteMode.SupportsReading] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[ReadWriteMode.SupportsReading] =
      (for {
        section ← config.v[com.typesafe.config.Config](path)
        rp ← section.v[ReadPreferenceAlm]("read-preference")
        wc ← section.magicOption[WriteConcernAlm]("write-concern")
      } yield wc match {
        case Some(wc) ⇒ ReadWriteMode.ReadAndWrite(rp, wc)
        case None     ⇒ ReadWriteMode.ReadOnly(rp)
      }).leftMap(p ⇒ ConfigurationProblem(s"""Failed to configure ReadWriteMode.SupportsReading @$path.""", cause = Some(p)))
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[ReadWriteMode.SupportsReading]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit val ReadWriteModeSupportsWritingConfigExtractorInst = new ConfigExtractor[ReadWriteMode.SupportsWriting] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[ReadWriteMode.SupportsWriting] =
      (for {
        section ← config.v[com.typesafe.config.Config](path)
        rp ← section.magicOption[ReadPreferenceAlm]("read-preference")
        wc ← section.v[WriteConcernAlm]("write-concern")
      } yield rp match {
        case Some(rp) ⇒ ReadWriteMode.ReadAndWrite(rp, wc)
        case None     ⇒ ReadWriteMode.WriteOnly(wc)
      }).leftMap(p ⇒ ConfigurationProblem(s"""Failed to configure ReadWriteMode.SupportsWriting @$path.""", cause = Some(p)))
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[ReadWriteMode.SupportsWriting]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }

  implicit val ReadWriteModeConfigExtractorInst = new ConfigExtractor[ReadWriteMode] {
    def getValue(config: com.typesafe.config.Config, path: String): AlmValidation[ReadWriteMode] =
      (for {
        section ← config.v[com.typesafe.config.Config](path)
        rp ← section.magicOption[ReadPreferenceAlm]("read-preference")
        wc ← section.magicOption[WriteConcernAlm]("write-concern")
      } yield ReadWriteMode(rp, wc)).leftMap(p ⇒ ConfigurationProblem(s"""Failed to configure ReadWriteMode @$path.""", cause = Some(p)))
    def tryGetValue(config: com.typesafe.config.Config, path: String): AlmValidation[Option[ReadWriteMode]] =
      config.opt[com.typesafe.config.Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }
}