package almhirt.akkax.reporting

import almhirt.akkax.ComponentState
import almhirt.common.CanCreateDateTime

/**
 * @author douven
 */
object StatusReportBuilders {
  def withName: String ⇒ StatusReport = (name) ⇒ StatusReport().withReportName (name)
  def withNameAndComponentState: String => ComponentState ⇒ StatusReport = name ⇒ state ⇒ StatusReport().withReportName(name).withComponentState(state)
  def withNameAndUtcCreationDate(implicit ccdt: CanCreateDateTime): String ⇒ StatusReport = (name) ⇒ StatusReport().withReportName(name).createdNowUtc
  def withNameAndCreationDate(implicit ccdt: CanCreateDateTime): String ⇒ StatusReport = (name) ⇒ StatusReport().withReportName(name).createdNow
  def withNameAndComponentStateAndUtcCreationDate(implicit ccdt: CanCreateDateTime): String => ComponentState ⇒ StatusReport = name ⇒ state ⇒ StatusReport().withReportName(name) .createdNowUtc
  def withNameAndComponentStateAndCreationDate(implicit ccdt: CanCreateDateTime): String => ComponentState ⇒ StatusReport = name ⇒ state ⇒ StatusReport().withReportName(name).withComponentState(state).createdNow
}