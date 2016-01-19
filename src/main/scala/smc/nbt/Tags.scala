package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.binary._
import smc.enum._
import smc.named._

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
  this: Enum =>

  // The Elem instance is defined below as TagDef[_].

  /** Defines the concrete set of type behavior definitions of this NBT system.
    *
    * Any types that a [[TypeDef]] is defined for are enabled
    * to be named-tagged and (de)serialized using [[GetPutTag]].
    *
    * Instances of [[TypeDef]] can be assumed to be
    * defined implicitly inside of [[Tags]].
    */
  type TypeDef[A] <: TypeDefAbs[A]
  /** Constrains each [[TypeDef]] instance to be mapped from/to a distinct number,
    * so that [[TypeDef]] instances work to specify their data type's ID as well.
    */
  protected override type EnumElement = TypeDef[_]

  /** Defines a given type's behavior in this NBT system.
    */
  protected trait TypeDefAbs[A] {

    /** Keeps track of the type info.
      */
    val typ: TypeTag[A]

    /** Specifies how to (de)serialize the given type's values.
      */
    val getput: GetPut[A]

    /** Specifies how to (de)serialize the given type's named tags' names.
      * It most certainly is the modified UTF-8 ([[GetPutString]]),
      * but some types could prefer some other methods instead.
      */
    val getputName: GetPut[String] = GetPutString
  }

  /** Sticks a value with a [[TypeDef]] of the data type,
    * to retrieve the data type's ID in constant time,
    * and to recover the data type without exception management.
    */
  final case class Typed[A](value: A)(implicit val tdef: TypeDef[A]) {

    /** Extracts the type-erased value if the specified type matches, otherwise [[None]].
      *
      * @throws NullPointerException if the specified [[TypeDef]] does not exist.
      */
    def castSafe[B: TypeDef]: Option[B] = {
      val tA = tdef.typ.tpe
      val tB = implicitly[TypeDef[B]].typ.tpe
      if (tA <:< tB) Some(castUnsafe[B]) else None
    }

    /** Extracts the type-erased value by casting the tag without type-checking.
      *
      * @throws ClassCastException   if the specified type is not matched.
      * @throws NullPointerException if the specified [[TypeDef]] does not exist.
      */
    def castUnsafe[B: TypeDef]: B = {
      if (implicitly[TypeDef[B]] == null)
        throw new NullPointerException
      else value.asInstanceOf[B]
    }

    override def toString = value.toString
  }

  /** Defines ID (de)serialization through [[TypeDef]]
    * that is constrained to be mapped from/to distinct numbers.
    */
  protected implicit object GetPutTypeDef extends GetPutAbs[TypeDef[_]] {
    override def get(i: DataInput): TypeDef[_] = element(i.readByte())

    override def put(o: DataOutput, n: TypeDef[_]): Unit = o.writeByte(ordinal(n))
  }

  /** Defines (de)serialization of named tags.
    *
    * @see [[smc.binary]] for the usage of this object.
    */
  implicit object GetPutTag extends GetPutAbs[Named[Typed[_]]] {
    override def get(in: DataInput): Named[Typed[_]] = {
      _read(in, GetPutTypeDef.get(in)) // IntelliJ error
    }

    // get put principle
    private def _read[A](in: DataInput, tdef: TypeDef[A]): Named[Typed[A]] = {
      (in.reads(tdef.getputName), Typed(in.reads(tdef.getput))(tdef))
    }

    override def put(out: DataOutput, ntag: Named[Typed[_]]) {
      _write(out, ntag.name, ntag.value)
    }

    // get put principle
    private def _write[A](out: DataOutput, name: String, tag: Typed[A]): Unit = {
      out.writes(tag.tdef)
      out.writes(name)(tag.tdef.getputName)
      out.writes(tag.value)(tag.tdef.getput)
    }
  }

}
