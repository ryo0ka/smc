package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.named._
import smc.polyio._
import smc.enum._

import scala.reflect.runtime.universe._

/** Provides essential functionality to convert data
  * from/to named binary tags and (de)serialize them.
  * {{{
  *   // Demonstrates quick construction of a NBT system and its usage.
  *   import smc.enum._
  *   import smc.named._
  *
  *   // Prepares whatever data type to (de)serialize.
  *   class Foo {...}
  *
  *   // Implements a NBT system with whatever data types.
  *   object TagsImpl extends Tags with DirtyEnum {
  *
  *     // Implements (de)serialization methods inside of TagsDef.
  *     class TagDefImpl[A] extends AbsTagDef[A] with DirtyElem {...}
  *     override type TagDef[A] = TagDefImpl[A]
  *
  *     // Lists up desired data types in desired order.
  *     val IntTag = new TagDefImpl[Int]
  *     val UTFTag = new TagDefImpl[String]
  *     val FooTag = new TagDefImpl[Foo]
  *   }
  *
  *   // Imports (de)serialization functionality of defined named tags.
  *   import TagsImpl._
  *
  *   // Imports polymorphic (de)serialization functionality.
  *   import smc.polyio._
  *
  *   // Prepares fake I/O sources.
  *   import java.io.{DataInput, DataOutput}
  *   val in: DataInput = ???
  *   val out:: DataOutput = ???
  *
  *   // Deserializes a Foo value from the input source (or fails).
  *   val ntag: Named[Tag[_]] = in.readP
  *   val foo: Foo = ntag.value.castUnsafe
  *
  *   // Serializes a Foo value to the output source.
  *   out.writeP(Tagged(foo) named "fooo")
  * }}}
  */
trait Tags {
  this: Enum => // The Elem instance is defined below as TagDef[_].

	/** Defines a given type's behavior in this NBT system.
	  */
	protected trait AbsTagDef[A] {

    /** Keeps track of the type info.
      */
		val ttag: TypeTag[A]

    /** Specifies how to (de)serialize the given type's values.
      */
		val valueIO: PolyIO[A]

    /** Specifies how to (de)serialize the given type's named tags' names.
      * It most certainly is the modified UTF-8 ([[StringIO]]),
      * but some types could prefer some other methods instead.
      */
		val nameIO: PolyIO[String] = StringIO
	}

	/** Defines the concrete set of type behavior definitions of this NBT system.
	  *
	  * Any types that a [[TagDef]] is defined for are enabled
    * to be named-tagged and (de)serialized using [[NamedTagIO]].
    *
    * Instances of [[TagDef]] can be assumed to be
    * defined implicitly inside of [[Tags]].
	  */
	type TagDef[A] <: AbsTagDef[A]

  /** Constrains each [[TagDef]] instance to be mapped from/to a distinct number,
    * so that [[TagDef]] instances work to specify their data type's ID as well.
    */
	protected override type Elem = TagDef[_]

  /** Defines ID (de)serialization through [[TagDef]]
    * that is constrained to be mapped from/to distinct numbers.
    */
	protected implicit object TagDefIO extends AbsPolyIO[TagDef[_]] {
		override def read(i: DataInput): TagDef[_] = element(i.readByte())
		override def write(o: DataOutput, n: TagDef[_]): Unit = o.writeByte(ordinal(n))
	}

	/** Sticks a value with a [[TagDef]] of the data type,
    * to retrieve the data type's ID in constant time,
    * and to recover the data type without exception management.
	  */
	final case class Tagged[A](value: A)(implicit val tdef: TagDef[A]) {

    /** Extracts the type-erased value by casting the tag without type-checking.
      * @throws ClassCastException if the specified type is not matched.
      * @throws NullPointerException if the specified [[TagDef]] does not exist.
      */
		def castUnsafe[B: TagDef]: B = {
      if (implicitly[TagDef[B]] == null)
        throw new NullPointerException
			else value.asInstanceOf[B]
		}

    /** Extracts the type-erased value if the specified type matches, otherwise [[None]].
      * @throws NullPointerException if the specified [[TagDef]] does not exist.
      */
		def castSafe[B: TagDef]: Option[B] = {
			val tA = tdef.ttag.tpe
			val tB = implicitly[TagDef[B]].ttag.tpe
			if (tA <:< tB) Some(castUnsafe[B]) else None
		}

		override def toString = value.toString
	}

	/** Defines (de)serialization of named tags.
    * @see [[smc.polyio]] for the usage of this object.
	  */
	implicit object NamedTagIO extends AbsPolyIO[Named[Tagged[_]]] {
		override def read(in: DataInput): Named[Tagged[_]] = {
			_read(in, TagDefIO.read(in)) // IntelliJ error
		}

		override def write(out: DataOutput, ntag: Named[Tagged[_]]) {
			_write(out, ntag.name, ntag.value)
		}

		// get put principle
		private def _read[A](in: DataInput, tdef: TagDef[A]): Named[Tagged[A]] = {
			(in.readP(tdef.nameIO), Tagged(in.readP(tdef.valueIO))(tdef))
		}

		// get put principle
		private def _write[A](out: DataOutput, name: String, tag: Tagged[A]): Unit = {
			out.writeP(tag.tdef)
			out.writeP(name)(tag.tdef.nameIO)
			out.writeP(tag.value)(tag.tdef.valueIO)
		}
	}
}
