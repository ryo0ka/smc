package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.named._
import smc.polyio._

import scala.collection._

/** Provides friendly functions and frameworks to [[Tags]].
  */
trait TagsUI { this: Tags =>

  /** Adds friendly methods to [[DataInput]].
    */
  implicit class TagInput(in: DataInput) {

    /** Reads a named binary tag.
      */
    def readNamedTag(): Named[Tagged[_]] = {
      in.readP(NamedTagIO)
    }
  }

  /** Adds friendly methods to [[DataOutput]].
    */
  implicit class TagOutput(o: DataOutput) {

    /** Writes a named binary tag.
      */
    def writeNamedTag(tag: Named[Tagged[_]]): Unit = {
      o.writeP(tag)(NamedTagIO)
    }
  }

  /** Adds friendly methods to values of types that a [[TagDef]] is defined for.
    */
	implicit class Taggable[A: TagDef](value: A) {

    /** Tags the value with the corresponding [[TagDef]].
      * {{{
      *   3.tagged() : Tagged[Int]
      * }}}
      */
		def tagged(): Tagged[A] = {
      Tagged[A](value)
    }
	}

  /** Defines a component by its type and name
    * to abstract out the tag construction and extraction.
    */
	class NamedTagDef[A: TagDef](val name: String) {
		/** Constructs a named tag of a given value.
      * {{{
      * val Version = new NamedTagDef[Int]("version")
      *
      * val v = Version(11933) : Named[Tag[Int]]
      *
      * out.writeNamedTag(v)
      * }}}
		 */
		def apply(t: A): Named[Tagged[A]] = {
			(name, Tagged(t))
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
		def unapply(n: Named[Tagged[_]]): Option[A] = n match {
			case (this.name, t) => t.castSafe[A]
			case _ => None
		}

		/** Looks for a desired value from a given map
      * whose keys are [[String]] and values are type-erased [[Tagged]].
      * {{{
      * val Version = new NamedTagDef[Int]("version")
      *
      * val ntags: Map[String, Tagged[_]] = ???
      *
      * val Version(v: Int) = ntags
      * }}}
		 */
		def unapply(m: Map[String, Tagged[_]]): Option[A] = {
			m.get(name).flatMap(_.castSafe[A])
		}
	}

  /** Adds friendly methods to [[TagDef]].
    */
	implicit class TagDefOps[A](tdef: TagDef[A]) {
		/** Constructs [[NamedTagDef]].
      * {{{
      * IntTag: Spec[Int] with SpecUI[Int] = ???
      * 
      * val Version = IntTag as "Version" : NamedTagDef[Int]
      * }}}
		  */
		def as(name: String): NamedTagDef[A] = {
      new NamedTagDef[A](name)(tdef)
    }
	}
}
