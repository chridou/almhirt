package riftwarp.http

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import almhirt.common._
import riftwarp._
import riftwarp.impl.UnsafeChannelRegistry

class RiftHttpContentTypeOpsSpecs extends FlatSpec with ShouldMatchers {
  val channelRegistry = {
    val registry = new UnsafeChannelRegistry()
    RiftChannel.register(registry)
    registry
  }

  val opsWithoutPrefix = new RiftHttpContentTypeWithoutPrefixOps(channelRegistry)

  "The safeHeaderValue method on RiftHttpContentTypeWithoutPrefixOps" should
    """create a string "text/json" for a RiftHttpChannelContentType(RiftChannel.Json, Map.empty) """ in {
      opsWithoutPrefix.safeHeaderValue(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)) should equal("text/json")
    }
  it should """create a string "text/json;encoding=utf-8" for a RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8")) """ in {
    opsWithoutPrefix.safeHeaderValue(RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8"))) should equal("text/json;encoding=utf-8")
  }
  it should """create a string "application/vnd.mytype+json" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)""" in {
    opsWithoutPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)) should equal("application/vnd.mytype+json")
  }
  it should """create a string "application/vnd.mytype+json;version=1" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)""" in {
    opsWithoutPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)) should equal("application/vnd.mytype+json;version=1")
  }
  it should """create a string "application/vnd.mytype+json;version=1;encoding=utf-8" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))""" in {
    opsWithoutPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))) should equal("application/vnd.mytype+json;version=1;encoding=utf-8")
  }

  "The parse method on RiftHttpContentTypeWithoutPrefixOps" should
    """create a Success(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)) on "text/json"""" in {
      opsWithoutPrefix.parse("text/json") should equal(scalaz.Success(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)))
    }
  it should """create a Success(RiftChannel.Json, Map("encoding" -> "utf-8")) on "text/json;encoding=utf-8"""" in {
    opsWithoutPrefix.parse("text/json;encoding=utf-8") should equal(scalaz.Success(RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8"))))
  }
  it should """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)) on "application/vnd.mytype+json"""" in {
    opsWithoutPrefix.parse("application/vnd.mytype+json") should equal(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)))
  }
  it should """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)) on "application/vnd.mytype+json;version=1"""" in {
    opsWithoutPrefix.parse("application/vnd.mytype+json;version=1") should equal(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)))
  }
  it should """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))) on "application/vnd.mytype+json;version=1;encoding=utf-8"""" in {
    opsWithoutPrefix.parse("application/vnd.mytype+json;version=1;encoding=utf-8") should equal(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))))
  }

  val opsWithPrefix = new RiftHttpContentTypeWithPrefixOps("prefix", channelRegistry)

  "The safeHeaderValue method on RiftHttpContentTypeWithPrefixOps and a prefix 'prefix'" should
    """create a string "text/json" for a RiftHttpChannelContentType(RiftChannel.Json, Map.empty) """ in {
      opsWithPrefix.safeHeaderValue(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)) should equal("text/json")
    }
  it should """create a string "text/json;encoding=utf-8" for a RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8")) """ in {
    opsWithPrefix.safeHeaderValue(RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8"))) should equal("text/json;encoding=utf-8")
  }
  it should """create a string "application/vnd.prefix.mytype+json" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)""" in {
    opsWithPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)) should equal("application/vnd.prefix.mytype+json")
  }
  it should """create a string "application/vnd.prefix.mytype+json;version=1" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)""" in {
    opsWithPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)) should equal("application/vnd.prefix.mytype+json;version=1")
  }
  it should """create a string "application/vnd.prefix.mytype+json;version=1;encoding=utf-8" for a RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))""" in {
    opsWithPrefix.safeHeaderValue(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))) should equal("application/vnd.prefix.mytype+json;version=1;encoding=utf-8")
  }

  "The parse method on RiftHttpContentTypeWithPrefixOps and a prefix 'prefix'" should
    """create a Success(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)) on "text/json"""" in {
      opsWithPrefix.parse("text/json") should equal(scalaz.Success(RiftHttpChannelContentType(RiftChannel.Json, Map.empty)))
    }
  it should """create a Success(RiftChannel.Json, Map("encoding" -> "utf-8")) on "text/json;encoding=utf-8"""" in {
    opsWithPrefix.parse("text/json;encoding=utf-8") should equal(scalaz.Success(RiftHttpChannelContentType(RiftChannel.Json, Map("encoding" -> "utf-8"))))
  }
  it should """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)) on "application/vnd.prefix.mytype+json"""" in {
    opsWithPrefix.parse("application/vnd.prefix.mytype+json") should equal(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype"), RiftChannel.Json, Map.empty)))
  }
  it should """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)) on "application/vnd.prefix.mytype+json;version=1"""" in {
    opsWithPrefix.parse("application/vnd.prefix.mytype+json;version=1") should equal(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map.empty)))
  }
  it should """create a Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))) on "application/vnd.prefix.mytype+json;version=1;encoding=utf-8"""" in {
    opsWithPrefix.parse("application/vnd.prefix.mytype+json;version=1;encoding=utf-8") should equal(scalaz.Success(RiftHttpQualifiedContentType(RiftDescriptor("mytype", 1), RiftChannel.Json, Map("encoding" -> "utf-8"))))
  }

  it should """fail on "application/vnd.wrong.mytype+json;version=1;encoding=utf-8"""" in {
    opsWithPrefix.parse("application/vnd.wrong.mytype+json;version=1;encoding=utf-8").isFailure should be(true)
  }

}