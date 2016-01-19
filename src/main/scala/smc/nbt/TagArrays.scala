package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.binary._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** Implements TAG_LIST specified in Named Binary Tag v19132.
  */
trait TagArrays {
  this: Tags =>

  /** Sticks an array with a [[TypeDef]] of the data type
    * to recover the data type without exception management.
    */
  case class TagArray[A](arr: Array[A])(implicit val tdef: TypeDef[A]) {

    /** Extracts the type-erased array if the specified type matches, otherwise [[None]].
      *
      * @throws NullPointerException if the specified [[TypeDef]] does not exist.
      */
    final def castSafe[B: TypeDef]: Option[Array[B]] = {
      val tA = tdef.typ.tpe
      val tB = implicitly[TypeDef[B]].typ.tpe
      if (tA <:< tB) Some(castUnsafe[B]) else None
    }

    /** Extracts the type-erased array by casting the tag without type-checking.
      *
      * @throws ClassCastException   if the specified type is not matched.
      * @throws NullPointerException if the specified [[TypeDef]] does not exist.
      */
    final def castUnsafe[B: TypeDef]: Array[B] = {
      if (implicitly[TypeDef[B]] == null)
        throw new NullPointerException
      else arr.asInstanceOf[Array[B]]
    }
  }

  /** Helps extract an [[Array]] from a [[Typed]] instance.
    *
    * It takes two processes to natively extract a typed array from a type-erased tag:
    * extract a type-erased tagged array from the type-erased tag,
    * and then extract a typed array from the type-erased tagged array.
    * This class's methods make it by one function call.
    */
  implicit class TagArrayOps(tag: Typed[_])(implicit tdef: TypeDef[TagArray[_]]) {

    /** Extracts the type-erased array by casting the tag without type-checking.
      *
      * @throws ClassCastException   if the specified type is not matched.
      * @throws NullPointerException if the specified [[TypeDef]] does not exist.
      */
    def castArrayUnsafe[A: TypeDef]: Array[A] = {
      tag.castUnsafe(tdef).castUnsafe[A]
    }

    /** Extracts the type-erased array if the specified type matches, otherwise [[None]].
      *
      * @throws NullPointerException if the specified [[TypeDef]] does not exist.
      */
    def castArraySafe[A: TypeDef]: Option[Array[A]] = {
      tag.castSafe(tdef).flatMap(_.castSafe[A])
    }
  }

  /** Defines (de)serialization of [[TagArray]].
    */
  protected object GetPutTagArray extends GetPutAbs[TagArray[_]] {
    override def get(i: DataInput) = _read(i, i.reads(GetPutTypeDef)) // IntelliJ error

    // get put principle
    private def _read[A](in: DataInput, tdef: TypeDef[A]): TagArray[A] = {
      val size = in.readInt()
      def data = in.reads(tdef.getput)
      val ctag = toCtag(tdef.typ)
      TagArray(Array.fill(size)(data)(ctag))(tdef)
    }

    private def toCtag[A](ttag: TypeTag[A]): ClassTag[A] = {
      ClassTag[A](ttag.mirror.runtimeClass(ttag.tpe))
    }

    override def put(o: DataOutput, s: TagArray[_]) = _write(o, s)

    // get put principle
    private def _write[A](out: DataOutput, tarr: TagArray[A]): Unit = {
      out.writes(tarr.tdef)(GetPutTypeDef) // IntelliJ error
      out.writeInt(tarr.arr.length)
      tarr.arr.foreach(out.writes(_)(tarr.tdef.getput))
    }
  }

}
