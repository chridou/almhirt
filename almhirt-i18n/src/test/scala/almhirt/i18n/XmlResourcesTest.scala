package almhirt.i18n

import almhirt.almvalidation.kit._
import org.scalatest._

class XmlResourcesTest extends FunSuite with Matchers {
  test("Files must be scanned from given resources folder") {
    val files = AlmResourcesHelper.getFilesInResources("localization", getClass.getClassLoader).forceResult
    files should have size (4)
  }

  test("The correct files must be returned") {
    val name = "test"
    val files = AlmResourcesHelper.getFilesInResourcesWithPattern("localization", s"$name.*\\.xml".r, getClass.getClassLoader).forceResult
    files should have size (3)
  }

}