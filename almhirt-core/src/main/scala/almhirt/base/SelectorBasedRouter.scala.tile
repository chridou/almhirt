package almhirt.base

import java.util.{ UUID => JUUID }
import akka.actor._

abstract class SelectorBasedRouter(val numChildren: Int, childProps: Props) extends Actor {
  type TSelect
  
  val children = (for (i <- 0 until (numChildren)) yield context.actorOf(childProps)).toVector
  
  def select(selector: TSelect): Int

  protected def dispatch(selector: TSelect, message: Any) {
    children(select(selector)) forward message
  }

}

trait UuidBasedRouter { self : SelectorBasedRouter => 
  type TSelect = JUUID

  def numChildren: Int
  
  override def select(selector: JUUID) = Math.abs(selector.hashCode()) % numChildren
}

trait LongBasedRouter { self : SelectorBasedRouter => 
  type TSelect = Long

  def numChildren: Int
  
  override def select(selector: Long) = Math.abs(selector.hashCode()) % numChildren
}

trait StringBasedRouter { self : SelectorBasedRouter => 
  type TSelect = String

  def numChildren: Int
  
  override def select(selector: String) = Math.abs(selector.hashCode()) % numChildren
}