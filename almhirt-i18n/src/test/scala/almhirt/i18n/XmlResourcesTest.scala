package almhirt.i18n

import scalaz._, Scalaz._
import almhirt.almvalidation.kit._
import org.scalatest._

class XmlResourcesTest extends FunSuite with Matchers {
  test("The correct files must be returned") {
    val name = "test"
    val files = AlmClassLoaderResourcesHelper.getFilesToLoad("localization", name, getClass.getClassLoader).forceResult
    files should have size (6)
  }

  test("The resources must be created") {
    val resources = AlmResources.fromXmlInResources("localization", "test", getClass.getClassLoader).forceResult
    info(resources.localesTree.map(n => s"${n.getBaseName} / ${n.getDisplayLanguage} / ${n.getDisplayCountry}").drawTree)
    resources.supportedLocales should have size(6)
  }
}