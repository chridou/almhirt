package almhirt.docit

import org.specs2.mutable._
import scalaz._, Scalaz._

object DocSample {
  lazy val l1 =
    DocTreeNode(
      ResourceDoc(
        "l1",
        "Title l1",
        "Description l1",
        None,
        false,
        Nil),
      List(l2a, l2b))

  
  
  lazy val l2a = 
    DocTreeNode(
      ResourceDoc(
        "l2a",
        "Title l2a",
        "Description l2a",
        None,
        false),
      Nil)
      
  lazy val l2b = 
    DocTreeNode(
      ResourceDoc(
        "l2b",
        "Title l2b",
        "Description l2b",
        Some("parmeter"),
        false),
      l3a :: l3b :: Nil)


  lazy val l3a =
    DocTreeNode(
      ResourceDoc(
        "l3a",
        "Title l3a",
        "Description l3a",
        None,
        false),
      Nil)

  lazy val l3b =
    DocTreeNode(
      ResourceDoc(
        "l3b",
        "Title l3b",
        "Description l3b",
        None,
        false),
      Nil)
}

class DocTreeNodeSpec extends Specification {
  import DocSample._
  """A single DocTreeNode(leaf)""" should {
    """be convertible to scalaz.Tree""" in {
      DocIt(l3b)
      success
    }
    """be convertible to scalaz.TreeLoc after being converted to a tree""" in {
      DocIt(l3b).loc
      success
    }
  }

  """A DocTreeNode(tree)""" should {
    """be convertible to scalaz.Tree""" in {
      DocIt(l1)
      success
    }
    """be convertible to scalaz.TreeLoc after being converted to a tree""" in {
      DocIt(l1).loc
      success
    }
  }
  
}