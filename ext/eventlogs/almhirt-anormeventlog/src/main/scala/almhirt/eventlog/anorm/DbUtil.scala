package almhirt.eventlog.anorm

import java.sql.Connection
import scalaz.syntax.validation._
import almhirt.common._
import java.sql.DriverManager

object DbUtil {
  def withConnection[T](getConnection: () => AlmValidation[Connection])(compute: Connection => AlmValidation[T]): AlmValidation[T] = {
    val connection = getConnection()
    connection.bind(conn => {
      try {
        val res = compute(conn)
        conn.close()
        res
      } catch {
        case exn =>
          PersistenceProblem("Could not execute a db operation", cause = Some(CauseIsThrowable(exn))).failure
      } finally {
        conn.close()
      }
    })
  }

  def inTransaction[T](withConnection: (Connection => AlmValidation[T]) => AlmValidation[T])(compute: Connection => AlmValidation[T]): AlmValidation[T] = {
    withConnection { conn =>
      val originalState = conn.getAutoCommit()
      conn.setAutoCommit(false)
      try {
        val res = compute(conn)
        conn.commit()
        res
      } catch {
        case exn =>
          conn.rollback
          PersistenceProblem("Could not execute transaction. Rolled back.", cause = Some(CauseIsThrowable(exn))).failure
      } finally {
        conn.setAutoCommit(originalState)
      }
    }
  }
}