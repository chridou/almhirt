/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.xml

import scala.xml.{Node, NodeSeq, Elem}
import scalaz.Validation
import scalaz.syntax.Ops
import almhirt._
import almhirt.almvalidation._

trait XmlOps0 extends Ops[Elem]{
  def extractInt(): AlmValidationSBD[Int] = 
    xml.funs.intFromXmlNode(self)
  def extractLong(): AlmValidationSBD[Long] = 
    xml.funs.longFromXmlNode(self)
  def extractDouble(): AlmValidationSBD[Double] = 
    xml.funs.doubleFromXmlNode(self)
  def extractOptionalInt(): AlmValidationSBD[Option[Int]] = 
    xml.funs.optionalIntXmlNode(self)
  def extractOptionalLong(): AlmValidationSBD[Option[Long]] = 
    xml.funs.optionalLongXmlNode(self)
  def extractOptionalDouble(): AlmValidationSBD[Option[Double]] = 
    xml.funs.optionalDoubleXmlNode(self)
  def extractStringFromChild(label: String): AlmValidationSBD[String] = 
    xml.funs.stringFromChild(self, label)
  def extractIntFromChild(label: String): AlmValidationSBD[Int] = 
    xml.funs.intFromChild(self, label)
  def extractLongFromChild(label: String): AlmValidationSBD[Long] = 
    xml.funs.longFromChild(self, label)
  def extractDoubleFromChild(label: String): AlmValidationSBD[Double] = 
    xml.funs.doubleFromChild(self, label)
  def extractOptionalStringFromChild(label: String): AlmValidationSBD[Option[String]] = 
    xml.funs.stringOptionFromChild(self, label)
  def extractOptionalIntFromChild(label: String): AlmValidationSBD[Option[Int]] = 
    xml.funs.intOptionFromChild(self, label)
  def extractOptionalLongFromChild(label: String): AlmValidationSBD[Option[Long]] = 
    xml.funs.longOptionFromChild(self, label)
  def extractOptionalDoubleFromChild(label: String): AlmValidationSBD[Option[Double]] =
    xml.funs.doubleOptionFromChild(self, label)
  def firstChildNode(label: String): AlmValidationSBD[Elem] = 
    xml.funs.firstChildNodeMandatory(self, label)
  def mapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
    xml.funs.mapOptionalFirstChild(self, label, compute)
  def flatMapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[Option[T]]): AlmValidationSBD[Option[T]] =
    xml.funs.flatMapOptionalFirstChild(self, label, compute)
  def mapOptionalFirstChildM[T](label: String, compute: Elem => AlmValidationMBD[T]): AlmValidationMBD[Option[T]] =
    xml.funs.mapOptionalFirstChildM(self, label, compute)
  def mapChildren[T](label: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[T]] =
    xml.funs.mapChildren(self, label, map)
  def mapChildrenWithAttribute[T](label: String, attName: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[(Option[String], T)]] =
    xml.funs.mapChildrenWithAttribute(self, label, attName, map)
  def \* = xml.funs.elems(self)
  def \#(label: String) = xml.funs.elems(self, label)
  def elems = xml.funs.elems(self)
}

trait ToXmlOps {
  implicit def FromElemToXmlOps0(a: Elem) = new XmlOps0{ def self = a }
}
