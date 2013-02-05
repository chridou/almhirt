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

import java.util.{UUID => JUUID}
import scala.language.implicitConversions
import scala.xml.{Node, NodeSeq, Elem}
import scalaz.Validation
import scalaz.syntax.Ops
import almhirt.common._
import almhirt.almvalidation._
import org.joda.time.DateTime

trait XmlOps0 extends Ops[Elem]{
  def extractString(): AlmValidation[String] =
    almhirt.almvalidation.funs.notEmptyOrWhitespace(self.text)
  def extractOptionalString(): Option[String] =
    extractString.toOption
  def extractInt(): AlmValidation[Int] = 
    funs.intFromXmlNode(self)
  def extractOptionalInt(): AlmValidation[Option[Int]] = 
    funs.optionalIntFromXmlNode(self)
  def extractLong(): AlmValidation[Long] = 
    funs.longFromXmlNode(self)
  def extractOptionalLong(): AlmValidation[Option[Long]] = 
    funs.optionalLongFromXmlNode(self)
  def extractDouble(): AlmValidation[Double] = 
    funs.doubleFromXmlNode(self)
  def extractOptionalDouble(): AlmValidation[Option[Double]] = 
    funs.optionalDoubleFromXmlNode(self)
  def extractDateTime(): AlmValidation[DateTime] = 
    funs.dateTimeFromXmlNode(self)
  def extractOptionalDateTime(): AlmValidation[Option[DateTime]] = 
    funs.optionalDateTimeFromXmlNode(self)
  def extractUuid(): AlmValidation[JUUID] = 
    funs.uuidFromXmlNode(self)
  def extractOptionalUuid(): AlmValidation[Option[JUUID]] = 
    funs.optionalUuidFromXmlNode(self)
    
  def extractStringFromChild(label: String): AlmValidation[String] = 
    funs.stringFromChild(self, label)
  def extractOptionalStringFromChild(label: String): AlmValidation[Option[String]] = 
    funs.stringOptionFromChild(self, label)
  def extractIntFromChild(label: String): AlmValidation[Int] = 
    funs.intFromChild(self, label)
  def extractOptionalIntFromChild(label: String): AlmValidation[Option[Int]] = 
    funs.intOptionFromChild(self, label)
  def extractLongFromChild(label: String): AlmValidation[Long] = 
    funs.longFromChild(self, label)
  def extractOptionalLongFromChild(label: String): AlmValidation[Option[Long]] = 
    funs.longOptionFromChild(self, label)
  def extractDoubleFromChild(label: String): AlmValidation[Double] = 
    funs.doubleFromChild(self, label)
  def extractOptionalDoubleFromChild(label: String): AlmValidation[Option[Double]] =
    funs.doubleOptionFromChild(self, label)
  def extractDateTimeFromChild(label: String): AlmValidation[DateTime] = 
    funs.dateTimeFromChild(self, label)
  def extractOptionalDateTimeFromChild(label: String): AlmValidation[Option[DateTime]] = 
    funs.dateTimeOptionFromChild(self, label)
  def extractUuidFromChild(label: String): AlmValidation[JUUID] = 
    funs.uuidFromChild(self, label)
  def extractOptionalUuidFromChild(label: String): AlmValidation[Option[JUUID]] =
    funs.uuidOptionFromChild(self, label)
    
    
  def firstChildNode(label: String): AlmValidation[Elem] = 
    funs.firstChildNodeMandatory(self, label)
  def mapOptionalFirstChild[T](label: String, compute: Elem => AlmValidation[T]): AlmValidation[Option[T]] =
    funs.mapOptionalFirstChild(self, label, compute)
  def flatMapOptionalFirstChild[T](label: String, compute: Elem => AlmValidation[Option[T]]): AlmValidation[Option[T]] =
    funs.flatMapOptionalFirstChild(self, label, compute)
  def \*(label: String) = funs.elems(self)(label)
  def \!(label: String) = funs.getChild(self)(label)
  def \?(label: String) = funs.tryGetChild(self)(label)
  def \??(label: String) = funs.tryGetChild(self)(label).fold(_ => None, succ => succ)
  def elems = funs.allElems(self)
  
  def \@(name: String): AlmValidation[String] = funs.getAttributeValue(self, name)
  def \@?(name: String): Option[String] = funs.getOptionalAttributeValue(self, name)
}

trait ToXmlOps {
  implicit def FromElemToXmlOps0(a: Elem) = new XmlOps0{ def self = a }
}
