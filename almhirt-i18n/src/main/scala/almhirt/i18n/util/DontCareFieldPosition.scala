package almhirt.i18n.util

import java.text.FieldPosition

/**
 * DontCareFieldPosition is a subclass of FieldPosition that indicates that the
 * caller is not interested in the start and end position of any field.
 * <p>
 * DontCareFieldPosition is a singleton, and its instance is immutable.
 * <p>
 * A <code>format</code> method use <code>fpos == DontCareFieldPosition.INSTANCE</code>
 * to tell whether or not it needs to calculate a field position.
 *
 */
// Pick some random number to be sure that we don't accidentally match with
// a field.
object DontCareFieldPosition extends FieldPosition(-913028704) {
  override def setBeginIndex(i: Int) {
    // Do nothing
  }

  override def setEndIndex(i: Int) {
    // Do nothing
  }
}