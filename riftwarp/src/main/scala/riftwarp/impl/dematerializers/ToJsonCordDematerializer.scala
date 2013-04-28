package riftwarp.impl.dematerializers

import language.higherKinds
import java.util.{ UUID => JUUID }
import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.collection.IterableLike
import org.joda.time.DateTime
import scalaz._, Scalaz._
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
import almhirt.almvalidation.kit._
import almhirt.common._
import riftwarp._

object ToJsonCordDematerializer extends DematerializerTemplate[DimensionCord] {
  protected def valueReprToDim(repr: Cord): DimensionCord =
    DimensionCord(repr)

  protected override final def getPrimitiveRepr(prim: WarpPrimitive): Cord =
    prim match {
      case WarpBoolean(value) => Cord(value.toString)
      case WarpString(value) => Cord(mapStringLike(launderString(value)))
      case WarpByte(value) => Cord(value.toString)
      case WarpInt(value) => Cord(value.toString)
      case WarpLong(value) => Cord(value.toString)
      case WarpBigInt(value) => Cord(mapStringLike(value.toString))
      case WarpFloat(value) => Cord(value.toString)
      case WarpDouble(value) => Cord(value.toString)
      case WarpBigDecimal(value) => Cord(mapStringLike(value.toString))
      case WarpUuid(value) => Cord(mapStringLike(value.toString))
      case WarpUri(value) => Cord(mapStringLike(launderString(value.toString)))
      case WarpDateTime(value) => Cord(mapStringLike(value.toString))
    }

  protected def getObjectRepr(rd: Option[RiftDescriptor], elems: Vector[(String, Option[ValueRepr])]) =
    ???

  protected def foldReprs(elems: Traversable[ValueRepr]) =
    foldParts(elems.toList)

  protected def foldTupleReprs(tuple: (ValueRepr, ValueRepr)) =
    foldParts(tuple._1 :: tuple._2 :: Nil)

  protected def foldTreeRepr(tree: scalaz.Tree[ValueRepr]) =
    foldParts(tree.rootLabel :: foldParts(tree.subForest.map(foldTreeRepr).toList) :: Nil)

  protected def foldByteArrayRepr(bytes: Array[Byte]) =
    foldParts(bytes.map(b => Cord(b.toString)).toList)

  protected def foldBase64Repr(bytes: Array[Byte]) =
    Cord(org.apache.commons.codec.binary.Base64.encodeBase64String(bytes))

  /*
 * Parts of "launderString" are taken from Lift-JSON:
 * 
* Copyright 2009-2010 WorldWide Conferencing, LLC
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
  private def launderString(str: String): Cord = {
    val buf = new StringBuilder
    for (i <- 0 until str.length) {
      val c = str.charAt(i)
      buf.append(c match {
        case '"' => "\\\""
        case '\\' => "\\\\"
        case '\b' => "\\b"
        case '\f' => "\\f"
        case '\n' => "\\n"
        case '\r' => "\\r"
        case '\t' => "\\t"
        case c if ((c >= '\u0000' && c < '\u001f') || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) => "\\u%04x".format(c: Int)
        case c => c
      })
    }
    buf.toString
  }

  private def mapStringLike(part: Cord): Cord = '\"' -: part :- '\"'

  @tailrec
  private def createInnerJson(rest: List[Cord], acc: Cord): Cord =
    rest match {
      case Nil => Cord("[]")
      case last :: Nil => '[' -: (acc ++ last) :- ']'
      case h :: t => createInnerJson(t, acc ++ h :- ',')
    }

  def foldParts(items: List[Cord]): Cord = createInnerJson(items, Cord.empty)
}