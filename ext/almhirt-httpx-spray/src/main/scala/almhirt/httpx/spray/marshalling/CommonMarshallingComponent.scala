package almhirt.httpx.spray.marshalling

trait CommonMarshallingComponent {
  def commonMarshallers: HasCommonMarshallers 
  def commonUnmarshallers: HasCommonUnmarshallers 
}