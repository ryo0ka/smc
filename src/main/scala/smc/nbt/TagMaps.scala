package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.binary
import smc.binary._
import smc.named._

import scala.collection._
import scala.reflect.runtime.universe._

/** Implements TAG_COMPOUND defined in Named Binary Tag v19132.
  */
trait TagMaps {
  this: Tags =>

  /** Represents TAG_COMPOUND.
    */
  type TagMap = Map[String, Typed[_]]
  protected val EndDef: End

  /** Defines (de)serialization of [[End]] tags.
    */
  protected trait End {
    this: TypeDef[End.type] =>
    final override val getput = binary.getput[End.type](_ => End, (_, _) => Unit)
    final override val getputName = binary.getput[String](_ => "", (_, _) => Unit)
    final override val typ = typeTag[End.type]

    /** Prepares a named [[End]] tag, since [[End]] tags do not have a name.
      */
    final val named: Named[Typed[End.type]] = Typed(End)(this) named "" // IntelliJ error
  }

  /** Thrown if a map contains any illegally placed TAG_END's.
    */
  final class MisplacedEndException(val map: TagMap) extends RuntimeException

  /** Represents TAG_END contents which is empty.
    */
  object End

  /** Defines (de)serialization of [[TagMap]].
    */
  protected object GetPutTagMap extends GetPutAbs[TagMap] {
    override def get(in: DataInput) = {
      Iterator.continually(GetPutTag.get(in)).takeWhile(!isEnd(_)).toMap
    }

    /** @throws MisplacedEndException if any [[End]] tags appear at unexpected position.
      */
    override def put(out: DataOutput, map: TagMap) = {
      map.foreach {
        case n if isEnd(n) => throw new MisplacedEndException(map)
        case n => out.writes(n)
      }
      out.writes(EndDef.named)
    }

    // Whether the given smc.named tag is of End or not.
    private def isEnd(nt: Named[Typed[_]]): Boolean = {
      nt.value.tdef.isInstanceOf[End]
    }
  }

}
