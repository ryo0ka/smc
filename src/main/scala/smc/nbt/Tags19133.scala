package smc.nbt

import smc.binary._
import smc.enum._

import scala.reflect.runtime.universe._

/** Named Binary Tag v19133 implementation.
  */
object Tags19133 extends Tags with TagMaps with TagArrays with TagsOps with DirtyEnum {


  protected def impl[A: TypeTag](gp: GetPut[A]) = new TypeDefImpl[A] {
    override val getput = gp
    override val typ = typeTag[A]
  }
  override type TypeDef[A] = TypeDefImpl[A]
  implicit override val EndDef = new TypeDefImpl[End.type] with End
  implicit val ByteDef = impl(GetPutByte)
  implicit val ShortDef = impl(GetPutShort)
  implicit val IntDef = impl(GetPutInt)
  implicit val LongDef = impl(GetPutLong)
  implicit val FloatDef = impl(GetPutFloat)
  implicit val DoubleDef = impl(GetPutDouble)
  implicit val BytesDef = impl(new GetPutArray(GetPutByte))
  implicit val StringDef = impl(GetPutString)
  implicit val ArrayDef = impl(GetPutTagArray)
  implicit val MapDef = impl(GetPutTagMap)
  implicit val IntsDef = impl(new GetPutArray(GetPutInt))

  protected trait TypeDefImpl[A] extends TypeDefAbs[A] with DirtyElement
}
