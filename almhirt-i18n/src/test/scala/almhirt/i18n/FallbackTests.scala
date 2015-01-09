package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import org.scalatest._
import com.ibm.icu.util.ULocale

class FallbackTests extends FunSpec with Matchers {
  val resAEnXml = <localized locale="en" root="true">
                    <section name="section">
                      <group name="group">
                        <key name="shared">
                          <plain>shared_en_a</plain>
                        </key>
                        <key name="not_shared_a">
                          <plain>not_shared_a_en</plain>
                        </key>
                      </group>
                    </section>
                  </localized>

  val resADeXml = <localized locale="de">
                    <section name="section">
                      <group name="group">
                        <key name="shared">
                          <plain>shared_de_a</plain>
                        </key>
                        <key name="not_shared_a">
                          <plain>not_shared_a_de</plain>
                        </key>
                      </group>
                    </section>
                  </localized>

  val resAXmls = resAEnXml :: resADeXml :: Nil

  val resBEnXml = <localized locale="en" root="true">
                    <section name="section">
                      <group name="group">
                        <key name="shared">
                          <plain>shared_en_b</plain>
                        </key>
                        <key name="not_shared_b">
                          <plain>not_shared_b_en</plain>
                        </key>
                      </group>
                    </section>
                  </localized>

  val resBFrXml = <localized locale="fr">
                    <section name="section">
                      <group name="group">
                        <key name="shared">
                          <plain>shared_fr_b</plain>
                        </key>
                        <key name="not_shared_b">
                          <plain>not_shared_b_fr</plain>
                        </key>
                      </group>
                    </section>
                  </localized>

  val resBXmls = resBEnXml :: resBFrXml :: Nil

  val group = ResourceGroup("section", "group")

  val keyShared = group.withKey("shared")
  val keyNotSharedA = group.withKey("not_shared_a")
  val keyNotSharedB = group.withKey("not_shared_b")

  describe("""Two resources A and B, that have the locales "en", "de", "fr"""") {
    describe("when A uses B as a fallback") {
      val resources = AlmResources.fromXml(resAXmls, false, false, false).flatMap(a ⇒ AlmResources.fromXml(resBXmls, false, false, false).flatMap(a.withFallback(_))).forceResult

      describe("""and the locale is "en"""") {
        val loc = new ULocale("en")
        it(s"""should return "shared_en_a" for $keyShared""") {
          resources.rawText(keyShared, loc) should equal("shared_en_a")
        }
        it(s"""should return "not_shared_a_en" for $keyNotSharedA""") {
          resources.rawText(keyNotSharedA, loc) should equal("not_shared_a_en")
        }
        it(s"""should return "not_shared_b_en" for $keyNotSharedB""") {
          resources.rawText(keyNotSharedB, loc) should equal("not_shared_b_en")
        }
      }
      describe("""and the locale is "de"""") {
        val loc = new ULocale("de")
        it(s"""should return "shared_de_a" for $keyShared""") {
          resources.rawText(keyShared, loc) should equal("shared_de_a")
        }
        it(s"""should return "not_shared_a_de" for $keyNotSharedA""") {
          resources.rawText(keyNotSharedA, loc) should equal("not_shared_a_de")
        }
        it(s"""should return NONE for $keyNotSharedB""") {
          resources.getRawText(keyNotSharedB, loc).toOption should equal(None)
        }

      }
      describe("""and the locale is "fr"""") {
        val loc = new ULocale("fr")
        it(s"""should return "shared_fr_a" for $keyShared""") {
          resources.rawText(keyShared, loc) should equal("shared_fr_b")
        }
        it(s"""should return NONE for $keyNotSharedA""") {
          resources.getRawText(keyNotSharedA, loc).toOption should equal(None)
        }
        it(s"""should return ""not_shared_b_fr" for $keyNotSharedB""") {
          resources.rawText(keyNotSharedB, loc) should equal("not_shared_b_fr")
        }
      }

    }
    describe("when B uses A as a fallback") {
      val resources = AlmResources.fromXml(resBXmls, false, false, false).flatMap(b ⇒ AlmResources.fromXml(resAXmls, false, false, false).flatMap(b.withFallback(_))).forceResult

      describe("""and the locale is "en"""") {
        val loc = new ULocale("en")
        it(s"""should return "shared_en_b" for $keyShared""") {
          resources.rawText(keyShared, loc) should equal("shared_en_b")
        }
        it(s"""should return "not_shared_a_en" for $keyNotSharedA""") {
          resources.rawText(keyNotSharedA, loc) should equal("not_shared_a_en")
        }
        it(s"""should return "not_shared_b_en" for $keyNotSharedB""") {
          resources.rawText(keyNotSharedB, loc) should equal("not_shared_b_en")
        }
      }
      describe("""and the locale is "de"""") {
        val loc = new ULocale("de")
        it(s"""should return "shared_de_a" for $keyShared""") {
          resources.rawText(keyShared, loc) should equal("shared_de_a")
        }
        it(s"""should return "not_shared_a_de" for $keyNotSharedA""") {
          resources.rawText(keyNotSharedA, loc) should equal("not_shared_a_de")
        }
        it(s"""should return NONE for $keyNotSharedB""") {
          resources.getRawText(keyNotSharedB, loc).toOption should equal(None)
        }
      }
      describe("""and the locale is "fr"""") {
        val loc = new ULocale("fr")
        it(s"""should return "shared_fr_b" for $keyShared""") {
          resources.rawText(keyShared, loc) should equal("shared_fr_b")
        }
        it(s"""should return NONE for $keyNotSharedA""") {
          resources.getRawText(keyNotSharedA, loc).toOption should equal(None)
        }
        it(s"""should return "not_shared_b_fr" for $keyNotSharedB""") {
          resources.rawText(keyNotSharedB, loc) should equal("not_shared_b_fr")
        }
      }
    }
  }
}