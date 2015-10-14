package riftwarpx.almhirt.serialization.core

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp
import almhirt.akkax._

object AppNameWarpPackaging extends WarpPacker[AppName] with RegisterableWarpPacker with RegisterableWarpUnpacker[AppName] {
  val warpDescriptor = WarpDescriptor("AppName")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[AppName]) :: Nil
  override def pack(what: AppName)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("name", what.value)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[AppName] =
    withFastLookUp(from) { lookup ⇒
      for {
        name ← lookup.getAs[String]("name")
      } yield AppName(name)
    }
}

object ComponentNameWarpPackaging extends WarpPacker[ComponentName] with RegisterableWarpPacker with RegisterableWarpUnpacker[ComponentName] {
  val warpDescriptor = WarpDescriptor("ComponentName")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ComponentName]) :: Nil
  override def pack(what: ComponentName)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("name", what.value)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[ComponentName] =
    withFastLookUp(from) { lookup ⇒
      for {
        name ← lookup.getAs[String]("name")
      } yield ComponentName(name)
    }
}

object ComponentIdWarpPackaging extends WarpPacker[ComponentId] with RegisterableWarpPacker with RegisterableWarpUnpacker[ComponentId] {
  val warpDescriptor = WarpDescriptor("ComponentId")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ComponentId]) :: Nil
  override def pack(what: ComponentId)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("app", what.app.value) ~> P("component", what.component.value)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[ComponentId] =
    withFastLookUp(from) { lookup ⇒
      for {
        app ← lookup.getAs[String]("app")
        component ← lookup.getAs[String]("component")
      } yield ComponentId(AppName(app), ComponentName(component))
    }
}

object GlobalComponentIdWarpPackaging extends WarpPacker[GlobalComponentId] with RegisterableWarpPacker with RegisterableWarpUnpacker[GlobalComponentId] {
  val warpDescriptor = WarpDescriptor("GlobalComponentId")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[GlobalComponentId]) :: Nil
  override def pack(what: GlobalComponentId)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~> P("node", what.node.value) ~> P("app", what.app.value) ~> P("component", what.component.value)
  }

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[GlobalComponentId] =
    withFastLookUp(from) { lookup ⇒
      for {
        node ← lookup.getAs[String]("node")
        app ← lookup.getAs[String]("app")
        component ← lookup.getAs[String]("component")
      } yield GlobalComponentId(NodeName(node), AppName(app), ComponentName(component))
    }
}