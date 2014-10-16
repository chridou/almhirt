package com.example.sillydemo.crazysheep

import almhirt.akkax._

trait CrazySheepActor extends AlmActor {
  private object DefaultComponentIdProvider extends ActorComponentIdProvider {
    def componentId = ComponentId(AppName("crazy-sheep"), ComponentName(self.path.name))
  }

  implicit override def componentNameProvider: ActorComponentIdProvider = DefaultComponentIdProvider

}