package almhirt.i18n

import almhirt.common._

trait HasResourceKey {
  def resourceKey: ResourceKey
}

trait PreparesFormatable {
  def prepare(fmt: Formatable, getFormatable: ResourceKey => AlmValidation[Formatable]): AlmValidation[Formatable]
}

trait ItemFormatter[T] extends HasResourceKey with PreparesFormatable