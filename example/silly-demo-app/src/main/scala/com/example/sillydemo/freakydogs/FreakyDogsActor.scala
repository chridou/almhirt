package com.example.sillydemo.freakydogs

import almhirt.akkax._

trait FreakyDogsActor extends AlmActor {
  private object DefaultComponentIdProvider extends ActorComponentIdProvider {
    def componentId = ComponentId(AppName("freaky-dogs"), ComponentName(self.path.name))
  }

  implicit override def componentNameProvider: ActorComponentIdProvider = DefaultComponentIdProvider

}