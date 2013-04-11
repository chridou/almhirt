package almhirt.ext.core.slick.eventlogs

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scala.slick.session.Database
import scala.slick.driver.H2Driver
import almhirt.common.HasExecutionContext
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared.Profiles

class TextEventLogRowTests extends FunSuite with MustMatchers {
  import scala.language.implicitConversions
  implicit def dateTimeToTimeStamp(dateTime: DateTime): java.sql.Timestamp = almhirt.ext.core.slick.TypeConversion.dateTimeToTimeStamp(dateTime)
  implicit val hasExecutionContext: HasExecutionContext = HasExecutionContext.single
  def withIsolatedDal[T](f: (EventLogStoreComponent[TextEventLogRow]) => T): T = {
    val dal = Profiles.createTextEventLogAccess("H2", "EVENTS", driver => Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver)).forceResult
    dal.create
    try {
      f(dal)
    } finally {
      dal.drop
    }
  }

  test("The dal must create the database") {
    withIsolatedDal(dal => {
      ()
    })
  }

  test("The dal must store a text row the database") {
    val textEventLogRow = TextEventLogRow(JUUID.randomUUID(), Some("the sender"), DateTime.now(), "TestEventType", "Nonsense", "The payload")
    withIsolatedDal(dal => {
      dal.insertEventRow(textEventLogRow)
    }).isSuccess
  }

  test("The dal must store 2 text rows the database") {
    val textEventLogRow1 = TextEventLogRow(JUUID.fromString("e762e519-66e2-4aee-9076-78bb508e46d5"), None, DateTime.now(), "TestEventType1", "Nonsense1", "The payload1")
    val textEventLogRow2 = TextEventLogRow(JUUID.fromString("b3102d20-0d7d-4267-af05-a7b2eff3c7c1"), None, DateTime.now(), "TestEventType2", "Nonsense2", "The payload2")
    withIsolatedDal(dal => {
      dal.insertEventRow(textEventLogRow1).flatMap(_ =>
        dal.insertEventRow(textEventLogRow2))
    }).isSuccess must be(true)
  }

  test("The dal must store 2 text rows the database and contain two rows afterwards") {
    val textEventLogRow1 = TextEventLogRow(JUUID.randomUUID(), None, DateTime.now(), "TestEventType1", "Nonsense1", "The payload1")
    val textEventLogRow2 = TextEventLogRow(JUUID.randomUUID(), Some("the sender"), DateTime.now(), "TestEventType2", "Nonsense2", "The payload2")
    withIsolatedDal(dal => {
      dal.insertEventRow(textEventLogRow1).flatMap { _ =>
        dal.insertEventRow(textEventLogRow2)
      }.forceResult

      val rowCount = dal.countEventRows
      rowCount.forceResult must equal(2)
    })
  }

  ignore("The dal must store a text row the database and retrieve the same row") {
    val textEventLogRow = TextEventLogRow(JUUID.randomUUID(), Some("the sender"), DateTime.now(), "TestEventType", "Nonsense", "The payload")
    val res =
      withIsolatedDal(dal => {
        dal.insertEventRow(textEventLogRow)
        dal.getEventRowById(textEventLogRow.id)
      }).forceResult
    res must equal(textEventLogRow)
  }

  ignore("The dal must store 2 text rows the database and retrieve both originals") {
    val textEventLogRow1 = TextEventLogRow(JUUID.randomUUID(), Some("the sender"), DateTime.now(), "TestEventType1", "Nonsense1", "The payload1")
    val textEventLogRow2 = TextEventLogRow(JUUID.randomUUID(), None, DateTime.now(), "TestEventType2", "Nonsense2", "The payload2")
    val (res1, res2) =
      withIsolatedDal(dal => {
        dal.insertEventRow(textEventLogRow1).flatMap { _ =>
          dal.insertEventRow(textEventLogRow2)
        }
        dal.getEventRowById(textEventLogRow1.id).flatMap(res1 =>
          dal.getEventRowById(textEventLogRow2.id).map(res2 =>
            (res1, res2)))
      }).forceResult

    res1 must equal(textEventLogRow1)
    res2 must equal(textEventLogRow2)
  }

}