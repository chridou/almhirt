package riftwarpx.messagepack

import java.io.ByteArrayOutputStream
import scalaz._, Scalaz._
import almhirt.almvalidation.kit._
import almhirt.common._
import riftwarp._
import riftwarp.std._
import org.msgpack.MessagePack
import org.msgpack.packer.Packer
import almhirt.converters.BinaryConverter

object ToMessagePackDematerializer extends Dematerializer[Array[Byte] @@ WarpTags.MessagePack] {
  override val channel = "messagepack"
  override val dimension = classOf[Array[Byte]].getName()

  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): Array[Byte] @@ WarpTags.MessagePack = {
    val messagePack = new MessagePack()
    val out = new ByteArrayOutputStream()
    val packer = messagePack.createPacker(out)
    transform(what, packer)
    WarpTags.MessagePack(out.toByteArray())
  }

  def transform(what: WarpPackage, packer: Packer) {
    what match {
      case prim: WarpPrimitive =>
        addPrimitiveRepr(prim, packer)
      case obj: WarpObject =>
        addObjectRepr(obj, packer: Packer)
      //      case WarpCollection(items) =>
      //        foldReprs(items.map(transform))
      //      case WarpAssociativeCollection(items) =>
      //        foldAssocRepr(items.map(item => (transform(item._1), transform(item._2))))
      //      case WarpTree(tree) =>
      //        foldTreeRepr(tree.map(transform))
      //      case WarpTuple2(a, b) =>
      //        foldTuple2Reprs((transform(a), transform(b)))
      //      case WarpTuple3(a, b, c) =>
      //        foldTuple3Reprs((transform(a), transform(b), transform(c)))
      //      case WarpBytes(bytes) =>
      //        foldByteArrayRepr(bytes)
      //      case WarpBlob(bytes) =>
      //        foldBlobRepr(bytes)
    }
  }

  private def addPrimitiveRepr(prim: WarpPrimitive, packer: Packer) {
    prim match {
      case WarpBoolean(value) => packer.write(value)
      case WarpString(value) => packer.write(value)
      case WarpByte(value) => packer.write(value)
      case WarpInt(value) => packer.write(value)
      case WarpLong(value) => packer.write(value)
      case WarpBigInt(value) => packer.write(value)
      case WarpFloat(value) => packer.write(value)
      case WarpDouble(value) => packer.write(value)
      case WarpBigDecimal(value) => packer.write(value.toString())
      case WarpUuid(value) => packer.write(BinaryConverter.uuidToBytes(value))
      case WarpUri(value) => packer.write(value.toString())
      case WarpDateTime(value) => packer.write(value.toString())
      case WarpLocalDateTime(value) => packer.write(value.toString())
      case WarpDuration(value) => packer.write(value.toString())
    }
  }

  private def addObjectRepr(warpObject: WarpObject, packer: Packer) {
    val theMap = new java.util.HashMap[String, Any]
     warpObject.warpDescriptor.foreach(wd => theMap.put(WarpDescriptor.defaultKey, wd.toParsableString(";")))
//    warpObject.elements.foreach{ case WarpElement(label, value) =>
//    	value.foreach(v => theMap.put(label, transform(v, )))
    }

}
  
//}":"${rd.toParsableString(";")}"""") ++ (if(warpObject.elements.isEmpty) Cord.empty else Cord(","))
//        case None => Cord("{")
//      }
//    val elements =
//      if (warpObject.elements.isEmpty)
//        Cord("")
//      else {
//        val items = warpObject.elements.map(elem => createElemRepr(elem))
//        items.drop(1).fold(items.head) { case (acc, x) => (acc :- ',') ++ x }
//      }
//    head ++ elements :- '}'
//  }
//
//  protected override def foldReprs(elems: Traversable[ValueRepr]): Cord =
//    foldParts(elems.toList)
//
//  protected override def foldTuple2Reprs(tuple: (ValueRepr, ValueRepr)): Cord =
//    foldParts(tuple._1 :: tuple._2 :: Nil)
//
//  protected override def foldTuple3Reprs(tuple: (ValueRepr, ValueRepr, ValueRepr)): Cord =
//    foldParts(tuple._1 :: tuple._2 :: tuple._3 :: Nil)
//    
//  protected override def foldAssocRepr(assoc: Traversable[(ValueRepr, ValueRepr)]): Cord =
//    foldParts(assoc.toList.map(x => foldTuple2Reprs(x)))
//    
//  protected override def foldTreeRepr(tree: scalaz.Tree[ValueRepr]): Cord =
//    foldParts(tree.rootLabel :: foldParts(tree.subForest.map(foldTreeRepr).toList) :: Nil)
//
//  protected override def foldByteArrayRepr(bytes: IndexedSeq[Byte]): Cord =
//    foldParts(bytes.map(b => Cord(b.toString)).toList)
//
//  protected override def foldBlobRepr(bytes: IndexedSeq[Byte]): Cord = 
//    getObjectRepr(Base64BlobWarpPacker.asWarpObject(bytes))
//
//  private def createElemRepr(elem: WarpElement): Cord =
//    elem.value match {
//      case Some(v) => s""""${elem.label}":""" + transform(v)
//      case None => s""""${elem.label}":null"""
//    }
//
//  /*
// * Parts of "launderString" are taken from Lift-JSON:
// * 
//* Copyright 2009-2010 WorldWide Conferencing, LLC
//*
//* Licensed under the Apache License, Version 2.0 (the "License");
//* you may not use this file except in compliance with the License.
//* You may obtain a copy of the License at
//*
//* http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing, software
//* distributed under the License is distributed on an "AS IS" BASIS,
//* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//* See the License for the specific language governing permissions and
//* limitations under the License.
//*/
//  private def launderString(str: String): Cord = {
//    val buf = new StringBuilder
//    for (i <- 0 until str.length) {
//      val c = str.charAt(i)
//      buf.append(c match {
//        case '"' => "\\\""
//        case '\\' => "\\\\"
//        case '\b' => "\\b"
//        case '\f' => "\\f"
//        case '\n' => "\\n"
//        case '\r' => "\\r"
//        case '\t' => "\\t"
//        case c if ((c >= '\u0000' && c < '\u001f') || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) => "\\u%04x".format(c: Int)
//        case c => c
//      })
//    }
//    buf.toString
//  }
//
//  private def mapStringLike(part: Cord): Cord = '\"' -: part :- '\"'
//
//  @tailrec
//  private def createInnerJson(rest: List[Cord], acc: Cord): Cord =
//    rest match {
//      case Nil => Cord("[]")
//      case last :: Nil => '[' -: (acc ++ last) :- ']'
//      case h :: t => createInnerJson(t, acc ++ h :- ',')
//    }
//
//  def foldParts(items: List[Cord]): Cord = createInnerJson(items, Cord.empty)
//}