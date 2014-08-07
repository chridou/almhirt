package almhirt.streaming

trait Supplier[TElement] {
  def supply(trader: SuppliesTrader[TElement]) 
}