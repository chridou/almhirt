package almhirt

trait CallbackSubscription {
  /** Does not block. Will be unsubscribed some time in the future! **/
  def cancel()
}