package almhirt.akkax.reporting

import almhirt.akkax.ComponentState
import almhirt.common.CanCreateDateTime

/**
 * @author douven
 */
object StatusReportBuilder {
  def withName: String ⇒ StatusReport = (name) ⇒ StatusReport(name)
  def withNameAndComponentState: String => ComponentState ⇒ StatusReport = name ⇒ state ⇒ StatusReport(name).withComponentState(state)
  def withNameAndUtcCreationDate(implicit ccdt: CanCreateDateTime): String ⇒ StatusReport = (name) ⇒ StatusReport(name).createdNowUtc
  def withNameAndCreationDate(implicit ccdt: CanCreateDateTime): String ⇒ StatusReport = (name) ⇒ StatusReport(name).createdNow
  def withNameAndComponentStateAndUtcCreationDate(implicit ccdt: CanCreateDateTime): String => ComponentState ⇒ StatusReport = name ⇒ state ⇒ StatusReport(name).withComponentState(state).createdNowUtc
  def withNameAndComponentStateAndCreationDate(implicit ccdt: CanCreateDateTime): String => ComponentState ⇒ StatusReport = name ⇒ state ⇒ StatusReport(name).withComponentState(state).createdNow
}