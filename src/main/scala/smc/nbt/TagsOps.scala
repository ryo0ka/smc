package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.binary._
import smc.named._

import scala.collection._

/** Provides friendly functions and frameworks to [[Tags]].
  */
trait TagsOps {
  this: Tags =>

  /** Adds friendly methods to [[DataInput]].
    */
  implicit class TagInput(in: DataInput) {

    /** Reads a named binary tag.
      */
    def readNamedTag(): Named[Typed[_]] = {
      in.reads(GetPutTag)
    }
  }

  /** Adds friendly methods to [[DataOutput]].
    */
  implicit class TagOutput(o: DataOutput) {

    /** Writes a named binary tag.
      */
    def writeNamedTag(tag: Named[Typed[_]]): Unit = {
      o.writes(tag)(GetPutTag)
    }
  }

  /** Defines a component by its type and name
    * to abstract out the tag construction and extraction.
    */
  class TagDef[A: TypeDef](val name: String) {
    /** Constructs a named tag of a given value.
      * {{{
      * val Version = new NamedTagDef[Int]("version")
      *
      * val v = Version(11933) : Named[Tag[Int]]
      *
      * out.writeNamedTag(v)
      * }}}
      */
    def apply(t: A): Named[Typed[A]] = {
      (name, Typed(t))
    }

    /** Attempts to extract a desired kind of value from a given named tag.
      * [[Nothing]] if the given tag's value is not qualified.
      * {{{
      * val Version = new NamedTagDef[Int]("version")
      *
      * val ntag = in.readNamedTag()
      *
      * val Version(v: Int) = ntag
      * }}}
      */
    def unapply(n: Named[Typed[_]]): Option[A] = n match {
      case (this.name, t) => t.castSafe[A]
      case _ => None
    }

    /** Looks for a desired value from a given map
      * whose keys are [[String]] and values are type-erased [[Typed]].
      * {{{
      * val Version = new NamedTagDef[Int]("version")
      *
      * val ntags: Map[String, Tagged[_]] = ???
      *
      * val Version(v: Int) = ntags
      * }}}
      */
    def unapply(m: Map[String, Typed[_]]): Option[A] = {
      m.get(name).flatMap(_.castSafe[A])
    }
  }

}
