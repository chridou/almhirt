package almhirt.i18n

import scalaz._, Scalaz._
import almhirt.almvalidation.kit._
import org.scalatest._

class XmlResourcesTest extends FunSuite with Matchers {
  test("Files must be scanned from given resources folder") {
    val files = AlmResourcesHelper.getFilesInResources("localization", getClass.getClassLoader).forceResult
    files should have size (7)
  }

  test("The correct files must be returned") {
    val name = "test"
    val files = AlmResourcesHelper.getFilesInResourcesWithPattern("localization", s"$name.*\\.xml".r, getClass.getClassLoader).forceResult
    files should have size (6)
  }

  test("The resources must be created") {
    val resources = AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader).forceResult
    info(resources.localesTree.map(n => s"${n.getBaseName} / ${n.getDisplayLanguage} / ${n.getDisplayCountry}").drawTree)
    resources.supportedLocales should have size(6)
  }
}