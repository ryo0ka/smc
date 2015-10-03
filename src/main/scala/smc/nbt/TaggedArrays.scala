package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.polyio._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** Implements TAG_LIST specified in Named Binary Tag v19132.
  */
trait TaggedArrays { this: Tags =>

  /** Sticks an array with a [[TagDef]] of the data type
    * to recover the data type without exception management.
    */
  case class TaggedArray[A](arr: Array[A])(implicit val tdef: TagDef[A]) {

    /** Extracts the type-erased array by casting the tag without type-checking.
      * @throws ClassCastException if the specified type is not matched.
      * @throws NullPointerException if the specified [[TagDef]] does not exist.
      */
    final def castUnsafe[B: TagDef]: Array[B] = {
      if (implicitly[TagDef[B]] == null)
        throw new NullPointerException
      else arr.asInstanceOf[Array[B]]
    }

    /** Extracts the type-erased array if the specified type matches, otherwise [[None]].
      * @throws NullPointerException if the specified [[TagDef]] does not exist.
      */
    final def castSafe[B: TagDef]: Option[Array[B]] = {
      val tA = tdef.ttag.tpe
      val tB = implicitly[TagDef[B]].ttag.tpe
      if (tA <:< tB) Some(castUnsafe[B]) else None
    }
  }

  /** Defines (de)serialization of [[TaggedArray]].
    */
  protected object TaggedArrayIO extends AbsPolyIO[TaggedArray[_]] {
    override def read(i: DataInput) = _read(i, i.readP(TagDefIO))
    override def write(o: DataOutput, s: TaggedArray[_]) = _write(o, s)

    // get put principle
    private def _read[A](in: DataInput, tdef: TagDef[A]): TaggedArray[A] = {
      val size = in.readInt()
      def data = in.readP(tdef.valueIO)
      val ctag = toCtag(tdef.ttag)
      TaggedArray(Array.fill(size)(data)(ctag))(tdef)
    }

    private def toCtag[A](ttag: TypeTag[A]): ClassTag[A] = {
      ClassTag[A](ttag.mirror.runtimeClass(ttag.tpe))
    }

    // get put principle
    private def _write[A](out: DataOutput, tarr: TaggedArray[A]): Unit = {
      out.writeP(tarr.tdef)(TagDefIO) // IntelliJ error
      out.writeInt(tarr.arr.length)
      tarr.arr.foreach(out.writeP(_)(tarr.tdef.valueIO))
    }
  }

  /** Helps extract an [[Array]] from a [[Tagged]] instance.
    *
    * It takes two processes to natively extract a typed array from a type-erased tag:
    * extract a type-erased tagged array from the type-erased tag,
    * and then extract a typed array from the type-erased tagged array.
    * This class's methods make it by one function call.
    */
  implicit class TagArrayOps(tag: Tagged[_])(implicit tdef: TagDef[TaggedArray[_]]) {

    /** Extracts the type-erased array by casting the tag without type-checking.
      * @throws ClassCastException if the specified type is not matched.
      * @throws NullPointerException if the specified [[TagDef]] does not exist.
      */
    def castArrayUnsafe[A: TagDef]: Array[A] = {
      tag.castUnsafe(tdef).castUnsafe[A]
    }

    /** Extracts the type-erased array if the specified type matches, otherwise [[None]].
      * @throws NullPointerException if the specified [[TagDef]] does not exist.
      */
    def castArraySafe[A: TagDef]: Option[Array[A]] = {
      tag.castSafe(tdef).flatMap(_.castSafe[A])
    }
  }
}
