package almhirt.eventlog.anorm

import java.sql.Connection
import scalaz.syntax.validation._
import almhirt.common._
import java.sql.DriverManager
import java.util.Properties

object DbUtil {
  def getConnection(url: String, props: Properties) = {
    try {
      DriverManager.getConnection(url, props).success
    } catch {
      case exn => PersistenceProblem("Could not connect to %s".format(url), cause = Some(CauseIsThrowable(exn))).failure
    }
  }

  def withConnection[T](getConnection: () => AlmValidation[Connection])(compute: Connection => AlmValidation[T]): AlmValidation[T] = {
    val connection = getConnection()
    connection.bind(conn => {
      try {
        val res = compute(conn)
        conn.close()
        res
      } catch {
        case exn =>
          PersistenceProblem("Could not execute a db operation: %s".format(exn.getMessage()), cause = Some(CauseIsThrowable(exn))).failure
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
        res.fold(problem => { conn.rollback(); problem.failure }, succ => { conn.commit(); succ.success })
      } catch {
        case exn =>
          conn.rollback
          PersistenceProblem("Could not commit transaction. Rolled back: %s".format(exn.getMessage()), cause = Some(CauseIsThrowable(exn))).failure
      } finally {
        conn.setAutoCommit(originalState)
      }
    }
  }

  def inTransactionWithConnection[T](getConnection: () => AlmValidation[Connection])(compute: Connection => AlmValidation[T]): AlmValidation[T] = {
    withConnection(getConnection) { conn =>
      val originalState = conn.getAutoCommit()
      conn.setAutoCommit(false)
      try {
        val res = compute(conn)
        res.fold(problem => { conn.rollback(); problem.failure }, succ => { conn.commit(); succ.success })
      } catch {
        case exn =>
          conn.rollback
          println(exn.getClass().getName())
          PersistenceProblem("Could not commit transaction. Rolled back: %s".format(exn.getMessage()), cause = Some(CauseIsThrowable(exn))).failure
      } finally {
        conn.setAutoCommit(originalState)
      }
    }
  }

}