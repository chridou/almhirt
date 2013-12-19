package almhirt.corex.spray.marshalling

trait CoreMarshallingComponent {
  def coreMarshallers: HasCoreMarshallers 
  def coreUnmarshallers: HasCoreUnmarshallers 
}