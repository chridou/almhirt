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
import almhirt.common._
import almhirt.almvalidation._

trait XmlOps0 extends Ops[Elem]{
  def extractInt(): AlmValidationSBD[Int] = 
    funs.intFromXmlNode(self)
  def extractLong(): AlmValidationSBD[Long] = 
    funs.longFromXmlNode(self)
  def extractDouble(): AlmValidationSBD[Double] = 
    funs.doubleFromXmlNode(self)
  def extractOptionalInt(): AlmValidationSBD[Option[Int]] = 
    funs.optionalIntXmlNode(self)
  def extractOptionalLong(): AlmValidationSBD[Option[Long]] = 
    funs.optionalLongXmlNode(self)
  def extractOptionalDouble(): AlmValidationSBD[Option[Double]] = 
    funs.optionalDoubleXmlNode(self)
  def extractStringFromChild(label: String): AlmValidationSBD[String] = 
    funs.stringFromChild(self, label)
  def extractIntFromChild(label: String): AlmValidationSBD[Int] = 
    funs.intFromChild(self, label)
  def extractLongFromChild(label: String): AlmValidationSBD[Long] = 
    funs.longFromChild(self, label)
  def extractDoubleFromChild(label: String): AlmValidationSBD[Double] = 
    funs.doubleFromChild(self, label)
  def extractOptionalStringFromChild(label: String): AlmValidationSBD[Option[String]] = 
    funs.stringOptionFromChild(self, label)
  def extractOptionalIntFromChild(label: String): AlmValidationSBD[Option[Int]] = 
    funs.intOptionFromChild(self, label)
  def extractOptionalLongFromChild(label: String): AlmValidationSBD[Option[Long]] = 
    funs.longOptionFromChild(self, label)
  def extractOptionalDoubleFromChild(label: String): AlmValidationSBD[Option[Double]] =
    funs.doubleOptionFromChild(self, label)
  def firstChildNode(label: String): AlmValidationSBD[Elem] = 
    funs.firstChildNodeMandatory(self, label)
  def mapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
    funs.mapOptionalFirstChild(self, label, compute)
  def flatMapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[Option[T]]): AlmValidationSBD[Option[T]] =
    funs.flatMapOptionalFirstChild(self, label, compute)
  def mapOptionalFirstChildM[T](label: String, compute: Elem => AlmValidationMBD[T]): AlmValidationMBD[Option[T]] =
    funs.mapOptionalFirstChildM(self, label, compute)
  def mapChildren[T](label: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[T]] =
    funs.mapChildren(self, label, map)
  def mapChildrenWithAttribute[T](label: String, attName: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[(Option[String], T)]] =
    funs.mapChildrenWithAttribute(self, label, attName, map)
  def \* = funs.elems(self)
  def \#(label: String) = funs.elems(self, label)
  def elems = funs.elems(self)
}

trait ToXmlOps {
  implicit def FromElemToXmlOps0(a: Elem) = new XmlOps0{ def self = a }
}
