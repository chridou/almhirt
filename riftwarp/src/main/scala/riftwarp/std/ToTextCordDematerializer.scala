//package riftwarp.std
//
//import scala.annotation.tailrec
//import scalaz._
//import scalaz.Cord
//import scalaz.Cord._
//import scalaz.std._
//import almhirt.almvalidation.kit._
//import almhirt.common._
//import riftwarp._
//
//object ToTextCordDematerializer extends Dematerializer[Cord @@ WarpTags.Text] {
//  val channel = "text"
//  val dimension = classOf[Cord].getName()
//
//  def transform(what: WarpPackage, level: Int): Cord =
//    what match {
//      case prim: WarpPrimitive =>
//        getPrimitiveRepr(prim)
//      case obj: WarpObject =>
//        getObjectRepr(obj)
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
//    }
//
//  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): Cord @@ WarpTags.Text =
//    valueReprToDim(transform(what, 0))
//
//  protected def valueReprToDim(repr: Cord): Cord @@ WarpTags.Text =
//    WarpTags.TextCord(repr)
//
//  protected final def getPrimitiveRepr(prim: WarpPrimitive): Cord =
//    Cord(prim.value.toString)
//
//  protected def getObjectRepr(warpObject: WarpObject): Cord = {
//    val head =
//      warpObject.warpDescriptor match {
//        case Some(rd) =>
//          Cord(s"""{"${WarpDescriptor.defaultKey}":"${rd.toParsableString(";")}"""") ++ (if (warpObject.elements.isEmpty) Cord.empty else Cord(","))
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
//  protected def foldReprs(elems: Traversable[Cord]): Cord =
//    foldParts(elems.toList)
//
//  protected def foldTuple2Reprs(tuple: (Cord, Cord)): Cord =
//    foldParts(tuple._1 :: tuple._2 :: Nil)
//
//  protected def foldTuple3Reprs(tuple: (Cord, Cord, Cord)): Cord =
//    foldParts(tuple._1 :: tuple._2 :: tuple._3 :: Nil)
//
//  protected def foldAssocRepr(assoc: Traversable[(Cord, Cord)]): Cord =
//    foldParts(assoc.toList.map(x => foldTuple2Reprs(x)))
//
//  protected def foldTreeRepr(tree: scalaz.Tree[Cord]): Cord =
//    foldParts(tree.rootLabel :: foldParts(tree.subForest.map(foldTreeRepr).toList) :: Nil)
//
//  protected def foldByteArrayRepr(bytes: IndexedSeq[Byte]): Cord =
//    foldParts(bytes.map(b => Cord(b.toString)).toList)
//
//  protected def foldBlobRepr(bytes: IndexedSeq[Byte]): Cord =
//    getObjectRepr(Base64BlobWarpPacker.asWarpObject(bytes))
//
//  private def createElemRepr(elem: WarpElement): Cord =
//    elem.value match {
//      case Some(v) => s""""${elem.label}":""" + transform(v)
//      case None => s""""${elem.label}":null"""
//    }
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