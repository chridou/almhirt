package almhirt.reactivemongox

import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.configuration._
import com.typesafe.config.ConfigFactory
import org.scalatest._

class ReadPreferenceConfigurationTests extends FunSuite with Matchers {
  test("Configure ReadPreferenceAlm.PrimaryOnly") {
    val cfgStr =
      """|read-preference {
         |   mode = primary-only
         |}""".stripMargin
    val cfg = ConfigFactory.parseString(cfgStr)
    val res = cfg.v[ReadPreferenceAlm]("read-preference").resultOrEscalate
    res should equal(ReadPreferenceAlm.PrimaryOnly)
  }

  test("Configure ReadPreferenceAlm.PrimaryPreferred") {
    val cfgStr =
      """|read-preference {
         |   mode = primary-preferred
         |}""".stripMargin
    val cfg = ConfigFactory.parseString(cfgStr)
    val res = cfg.v[ReadPreferenceAlm]("read-preference").resultOrEscalate
    res should equal(ReadPreferenceAlm.PrimaryPreferred())
  }

  test("Configure ReadPreferenceAlm.SecondaryPreferred") {
    val cfgStr =
      """|read-preference {
         |   mode = secondary-preferred
         |}""".stripMargin
    val cfg = ConfigFactory.parseString(cfgStr)
    val res = cfg.v[ReadPreferenceAlm]("read-preference").resultOrEscalate
    res should equal(ReadPreferenceAlm.SecondaryPreferred())
  }

  test("Configure ReadPreferenceAlm.SecondaryOnly") {
    val cfgStr =
      """|read-preference {
         |   mode = secondary-only
         |}""".stripMargin
    val cfg = ConfigFactory.parseString(cfgStr)
    val res = cfg.v[ReadPreferenceAlm]("read-preference").resultOrEscalate
    res should equal(ReadPreferenceAlm.SecondaryOnly())
  }

  test("Configure ReadPreferenceAlm.Nearest") {
    val cfgStr =
      """|read-preference {
         |   mode = nearest
         |}""".stripMargin
    val cfg = ConfigFactory.parseString(cfgStr)
    val res = cfg.v[ReadPreferenceAlm]("read-preference").resultOrEscalate
    res should equal(ReadPreferenceAlm.Nearest())
  }
}