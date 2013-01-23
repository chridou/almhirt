package riftwarp.impl

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class ConcurrentDimensionConverterRegistry extends HasDimensionConverters {
  private val converters = new _root_.java.util.concurrent.ConcurrentHashMap[String, RawDimensionConverter](128)
  private val convertersBySource = new _root_.java.util.concurrent.ConcurrentHashMap[String, List[RawDimensionConverter]](256)
  private val convertersByTarget = new _root_.java.util.concurrent.ConcurrentHashMap[String, List[RawDimensionConverter]](256)

  def addConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](converter: DimensionConverter[DimSource, DimTarget]) {
    val ident = converter.tSource.getName()+converter.tTarget.getName()
    converters.put(ident, converter)
    if(!convertersBySource.containsKey(converter.tSource.getName()))
      convertersBySource.put(converter.tSource.getName(), converter :: Nil)
    else {
      val current = convertersBySource.get(converter.tSource.getName())
      convertersBySource.put(converter.tSource.getName(), converter :: current)
    }
    if(!convertersByTarget.containsKey(converter.tTarget.getName())) 
      convertersByTarget.put(converter.tTarget.getName(), converter :: Nil)
    else {
      val current = convertersByTarget.get(converter.tTarget.getName())
      convertersByTarget.put(converter.tTarget.getName(), converter :: current)
    }
  }

  def tryGetConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](implicit mS: ClassTag[DimSource], mT: ClassTag[DimTarget]): Option[DimensionConverter[DimSource, DimTarget]] =
    converters.get(mS.runtimeClass.getName()+mT.runtimeClass.getName()) match {
      case null => None
      case x => Some(x.asInstanceOf[DimensionConverter[DimSource, DimTarget]])
   }

  def getConvertersFromByDimType(tSource: Class[_ <: RiftDimension]): List[DimensionConverter[_, _]] =
    convertersBySource.get(tSource) match {
      case null => Nil
      case x => x.asInstanceOf[List[DimensionConverter[_, _]]]
   }
    
  def getConvertersToByDimType(tTarget: Class[_ <: RiftDimension]): List[DimensionConverter[_, _]] =
    convertersByTarget.get(tTarget.getName()) match {
      case null => Nil
      case x => x.asInstanceOf[List[DimensionConverter[_, _]]]
   }
}