package riftwarp.http

import org.specs2.mutable._
import almhirt.common._
import riftwarp._
import riftwarp.impl.UnsafeChannelRegistry

class RiftHttpContentTypeOpsSpecs extends Specification {
  val channelRegistry = {
    val registry = new UnsafeChannelRegistry()
    RiftChannel.register(registry)
    registry
  }

  val opsWithoutPrefix = new RiftHttpContentTypeWithoutPrefixOps(channelRegistry)

  "The safeHeaderValue method on RiftHttpContentTypeWithoutPrefixOps" should {
    """create a string "text/json" for a RiftHttpChannelContentType(RiftChannel.Json, Map.empty) """ in {
      opsWithoutPrefix.safeHeaderValue(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)) === "text/json"
    }
    """create a string "text/json;encoding=utf-8" for a RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8")) """ in {
      opsWithoutPrefix.safeHeaderValue(RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8"))) === "text/json;encoding=utf-8"
    }
    """create a string "application/vnd.mytype+json" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)""" in {
      opsWithoutPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)) === "application/vnd.mytype+json"
    }
    """create a string "application/vnd.mytype+json;version=1" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)""" in {
      opsWithoutPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)) === "application/vnd.mytype+json;version=1"
    }
    """create a string "application/vnd.mytype+json;version=1;encoding=utf-8" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))""" in {
      opsWithoutPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))) === "application/vnd.mytype+json;version=1;encoding=utf-8"
    }
  }

  "The parse method on RiftHttpContentTypeWithoutPrefixOps" should {
    """create a Success(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)) on "text/json"""" in {
      opsWithoutPrefix.parse("text/json") must beEqualTo(scalaz.Success(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)))
    }
    """create a Success(RiftChannel.Json, Map("encoding" -> "utf-8")) on "text/json;encoding=utf-8"""" in {
      opsWithoutPrefix.parse("text/json;encoding=utf-8") must beEqualTo(scalaz.Success(RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8"))))
    }
    """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)) on "application/vnd.mytype+json"""" in {
      opsWithoutPrefix.parse("application/vnd.mytype+json") must beEqualTo(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)))
    }
    """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)) on "application/vnd.mytype+json;version=1"""" in {
      opsWithoutPrefix.parse("application/vnd.mytype+json;version=1") must beEqualTo(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)))
    }
    """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))) on "application/vnd.mytype+json;version=1;encoding=utf-8"""" in {
      opsWithoutPrefix.parse("application/vnd.mytype+json;version=1;encoding=utf-8") must beEqualTo(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))))
    }
  }

  val opsWithPrefix = new RiftHttpContentTypeWithPrefixOps("prefix", channelRegistry)

  "The safeHeaderValue method on RiftHttpContentTypeWithPrefixOps and a prefix 'prefix'" should {
    """create a string "text/json" for a RiftHttpChannelContentType(RiftChannel.Json, Map.empty) """ in {
      opsWithPrefix.safeHeaderValue(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)) === "text/json"
    }
    """create a string "text/json;encoding=utf-8" for a RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8")) """ in {
      opsWithPrefix.safeHeaderValue(RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8"))) === "text/json;encoding=utf-8"
    }
    """create a string "application/vnd.prefix.mytype+json" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)""" in {
      opsWithPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)) === "application/vnd.prefix.mytype+json"
    }
    """create a string "application/vnd.prefix.mytype+json;version=1" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)""" in {
      opsWithPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)) === "application/vnd.prefix.mytype+json;version=1"
    }
    """create a string "application/vnd.prefix.mytype+json;version=1;encoding=utf-8" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))""" in {
      opsWithPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))) === "application/vnd.prefix.mytype+json;version=1;encoding=utf-8"
    }
  }

  "The parse method on RiftHttpContentTypeWithPrefixOps and a prefix 'prefix'" should {
    """create a Success(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)) on "text/json"""" in {
      opsWithPrefix.parse("text/json") must beEqualTo(scalaz.Success(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)))
    }
    """create a Success(RiftChannel.Json, Map("encoding" -> "utf-8")) on "text/json;encoding=utf-8"""" in {
      opsWithPrefix.parse("text/json;encoding=utf-8") must beEqualTo(scalaz.Success(RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8"))))
    }
    """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)) on "application/vnd.prefix.mytype+json"""" in {
      opsWithPrefix.parse("application/vnd.prefix.mytype+json") must beEqualTo(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)))
    }
    """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)) on "application/vnd.prefix.mytype+json;version=1"""" in {
      opsWithPrefix.parse("application/vnd.prefix.mytype+json;version=1") must beEqualTo(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)))
    }
    """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))) on "application/vnd.prefix.mytype+json;version=1;encoding=utf-8"""" in {
      opsWithPrefix.parse("application/vnd.prefix.mytype+json;version=1;encoding=utf-8") must beEqualTo(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))))
    }

    """fail on "application/vnd.wrong.mytype+json;version=1;encoding=utf-8"""" in {
      opsWithPrefix.parse("application/vnd.wrong.mytype+json;version=1;encoding=utf-8").isFailure
    }
  
  }

}