package almhirt.ext.core.slick.eventlogs

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scala.slick.session.Database
import scala.slick.driver.H2Driver
import almhirt.almvalidation.kit._

class TextEventLogRowTests extends FunSuite with MustMatchers {
  def withIsolatedDal[T](f: (Database, TextEventLogDataAccess) => T): T = {
    val db = Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    //val db = Database.forURL("jdbc:h2:tcp://localhost/~/test;USER=testuser;PASSWORD=testuser", driver = "org.h2.Driver")
    val dal = new TextEventLogDataAccess("EVENTS", "BLOBS", H2Driver)
    import dal.profile.simple._
    db withSession { implicit session: Session =>
      dal.create
    }
    try {
      f(db, dal)
    } finally {
      db withSession { implicit session: Session =>
        dal.drop
      }

    }
  }

  test("The dal must create the database") {
    withIsolatedDal((db, dal) => {
      ()
    })
  }

  test("The dal must store a text row the database") {
    val textEventLogRow = TextEventLogRow(JUUID.randomUUID(), DateTime.now(), "TestEventType", "Nonsense", "The payload")
    withIsolatedDal((db, dal) => {
      import dal.profile.simple._
      db.withSession { implicit session: Session =>
        dal.insertEventRow(textEventLogRow)
      }
    }).isSuccess
  }

  test("The dal must store 2 text rows the database") {
    val textEventLogRow1 = TextEventLogRow(JUUID.fromString("e762e519-66e2-4aee-9076-78bb508e46d5"), DateTime.now(), "TestEventType1", "Nonsense1", "The payload1")
    val textEventLogRow2 = TextEventLogRow(JUUID.fromString("b3102d20-0d7d-4267-af05-a7b2eff3c7c1"), DateTime.now(), "TestEventType2", "Nonsense2", "The payload2")
    withIsolatedDal((db, dal) => {
      import dal.profile.simple._
      db.withSession { implicit session: Session =>
        dal.insertEventRow(textEventLogRow1)
        dal.insertEventRow(textEventLogRow2)
      }.forceResult
    })
  }

  test("The dal must store 2 text rows the database and contain two rows afterwards") {
    val textEventLogRow1 = TextEventLogRow(JUUID.randomUUID(), DateTime.now(), "TestEventType1", "Nonsense1", "The payload1")
    val textEventLogRow2 = TextEventLogRow(JUUID.randomUUID(), DateTime.now(), "TestEventType2", "Nonsense2", "The payload2")
    withIsolatedDal((db, dal) => {
      import dal.profile.simple._
      db.withSession { implicit session: Session =>
        dal.insertEventRow(textEventLogRow1).flatMap { _ =>
          dal.insertEventRow(textEventLogRow2)
        }.forceResult
      }

      db.withSession { implicit session: Session =>
        val rowCount = dal.countEventRows
        rowCount.forceResult must equal(2)
      }

    })
  }

  test("The dal must store a text row the database and retrieve the same row") {
    val textEventLogRow = TextEventLogRow(JUUID.randomUUID(), DateTime.now(), "TestEventType", "Nonsense", "The payload")
    val res =
      withIsolatedDal((db, dal) => {
        import dal.profile.simple._
        db.withSession { implicit session: Session =>
          dal.insertEventRow(textEventLogRow)
        }
        db.withSession { implicit session: Session =>
          dal.getEventRowById(textEventLogRow.id)
        }
      }).forceResult
    res must equal(textEventLogRow)
  }

  test("The dal must store 2 text rows the database and retrieve both originals") {
    val textEventLogRow1 = TextEventLogRow(JUUID.randomUUID(), DateTime.now(), "TestEventType1", "Nonsense1", "The payload1")
    val textEventLogRow2 = TextEventLogRow(JUUID.randomUUID(), DateTime.now(), "TestEventType2", "Nonsense2", "The payload2")
    val (res1, res2) =
      withIsolatedDal((db, dal) => {
        import dal.profile.simple._
        db.withSession { implicit session: Session =>
          dal.insertEventRow(textEventLogRow1).flatMap { _ =>
            dal.insertEventRow(textEventLogRow2)
          }
        }
        db.withSession { implicit session: Session =>
          dal.getEventRowById(textEventLogRow1.id).flatMap(res1 =>
            dal.getEventRowById(textEventLogRow2.id).map(res2 =>
              (res1, res2)))
        }
      }).forceResult

    res1 must equal(textEventLogRow1)
    res2 must equal(textEventLogRow2)
  }

}