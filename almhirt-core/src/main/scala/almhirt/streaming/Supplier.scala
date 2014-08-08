package almhirt.streaming

trait Supplier[TElement] {
  def signContract(trader: SuppliesBroker[TElement]) 
}