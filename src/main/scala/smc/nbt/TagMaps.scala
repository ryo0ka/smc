package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.named._
import smc.polyio._

import scala.collection._
import scala.reflect.runtime.universe._

/** Implements TAG_COMPOUND defined in Named Binary Tag v19132.
  */
trait TagMaps { this: Tags =>

  /** Represents TAG_END contents which is empty.
    */
  object End

  /** Defines (de)serialization of [[End]] tags.
    */
  protected trait End { this: TagDef[End.type] =>
    final override val valueIO = polyIO[End.type](_ => End, (_, _) => Unit)
    final override val nameIO = polyIO[String](_ => "", (_, _) => Unit)
    final override val ttag = typeTag[End.type]

    /** Prepares a named [[End]] tag, since [[End]] tags do not have a name.
      */
    final val named: Named[Tagged[End.type]] = Tagged(End)(this) named "" // IntelliJ error
  }

  /** Represents TAG_COMPOUND.
    */
  type TagMap = Map[String, Tagged[_]]

  /** Defines (de)serialization of [[TagMap]].
    */
  protected final class TagMapIO(implicit end: End) extends AbsPolyIO[TagMap] {
    // Whether the given smc.named tag is of End or not.
    private def isEnd(nt: Named[Tagged[_]]): Boolean = {
      nt.value.tdef.isInstanceOf[End]
    }

    override def read(in: DataInput) = {
      Iterator.continually(NamedTagIO.read(in)).takeWhile(!isEnd(_)).toMap
    }

    /** @throws MisplacedEndException if any [[End]] tags appear at unexpected position.
      */
    override def write(out: DataOutput, map: TagMap) = {
      map.foreach {
        case n if isEnd(n) => throw new MisplacedEndException(map)
        case n => out.writeP(n)
      }
      out.writeP(end.named)
    }
  }

  /** Thrown if a map contains any illegally placed TAG_END's.
    */
  final class MisplacedEndException(val map: TagMap) extends RuntimeException
}
