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
  def extractInt(): AlmValidation[Int] = 
    funs.intFromXmlNode(self)
  def extractLong(): AlmValidation[Long] = 
    funs.longFromXmlNode(self)
  def extractDouble(): AlmValidation[Double] = 
    funs.doubleFromXmlNode(self)
  def extractOptionalInt(): AlmValidation[Option[Int]] = 
    funs.optionalIntXmlNode(self)
  def extractOptionalLong(): AlmValidation[Option[Long]] = 
    funs.optionalLongXmlNode(self)
  def extractOptionalDouble(): AlmValidation[Option[Double]] = 
    funs.optionalDoubleXmlNode(self)
  def extractStringFromChild(label: String): AlmValidation[String] = 
    funs.stringFromChild(self, label)
  def extractIntFromChild(label: String): AlmValidation[Int] = 
    funs.intFromChild(self, label)
  def extractLongFromChild(label: String): AlmValidation[Long] = 
    funs.longFromChild(self, label)
  def extractDoubleFromChild(label: String): AlmValidation[Double] = 
    funs.doubleFromChild(self, label)
  def extractOptionalStringFromChild(label: String): AlmValidation[Option[String]] = 
    funs.stringOptionFromChild(self, label)
  def extractOptionalIntFromChild(label: String): AlmValidation[Option[Int]] = 
    funs.intOptionFromChild(self, label)
  def extractOptionalLongFromChild(label: String): AlmValidation[Option[Long]] = 
    funs.longOptionFromChild(self, label)
  def extractOptionalDoubleFromChild(label: String): AlmValidation[Option[Double]] =
    funs.doubleOptionFromChild(self, label)
  def firstChildNode(label: String): AlmValidation[Elem] = 
    funs.firstChildNodeMandatory(self, label)
  def mapOptionalFirstChild[T](label: String, compute: Elem => AlmValidation[T]): AlmValidation[Option[T]] =
    funs.mapOptionalFirstChild(self, label, compute)
  def flatMapOptionalFirstChild[T](label: String, compute: Elem => AlmValidation[Option[T]]): AlmValidation[Option[T]] =
    funs.flatMapOptionalFirstChild(self, label, compute)
  def \* = funs.elems(self)
  def \#(label: String) = funs.elems(self, label)
  def elems = funs.elems(self)
}

trait ToXmlOps {
  implicit def FromElemToXmlOps0(a: Elem) = new XmlOps0{ def self = a }
}
