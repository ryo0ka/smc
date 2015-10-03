package smc.nbt

import smc.polyio._
import smc.enum._

import scala.reflect.runtime.universe._

/** Named Binary Tag v19133 implementation.
  */
object Tags19133 extends Tags with TagMaps with TaggedArrays with TagsUI with DirtyEnum {

	protected trait S[A] extends AbsTagDef[A] with DirtyElem

	override type TagDef[A] = S[A]

	protected class SI[A: TypeTag](override val valueIO: PolyIO[A]) extends S[A] {
    override val ttag = typeTag[A]
  }

	implicit val EndTag    = new S[End.type] with End
	implicit val ByteTag   = new SI(ByteIO)
	implicit val ShortTag  = new SI(ShortIO)
	implicit val IntTag    = new SI(IntIO)
	implicit val LongTag   = new SI(LongIO)
	implicit val FloatTag  = new SI(FloatIO)
	implicit val DoubleTag = new SI(DoubleIO)
	implicit val BytesTag  = new SI(new ArrayIO(ByteIO))
	implicit val StringTag = new SI(StringIO)
	implicit val ArrayTag  = new SI(TaggedArrayIO)
	implicit val MapTag    = new SI(new TagMapIO)
	implicit val IntsTag   = new SI(new ArrayIO(IntIO))
}
