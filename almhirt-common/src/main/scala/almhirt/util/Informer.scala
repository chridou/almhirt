package almhirt.util

/**
 * @author douven
 */
trait Informer {
  def inform(importance: Importance, msg: => String)
  
  final def notImportant(msg: => String): Unit = this.inform(Importance.NotImportant, msg)
  final def slightlyImportant(msg: => String): Unit = this.inform(Importance.SlightlyImportant, msg)
  final def important(msg: => String): Unit = this.inform(Importance.Important, msg)
  final def veryImportant(msg: => String): Unit = this.inform(Importance.VeryImportant, msg)
}