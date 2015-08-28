package smc.nbt

import dataio._
import smc.nbt.api._
import scala.reflect.runtime.universe._

trait Tags19132 extends Tags with Ends with TagMaps with TagSeqs with SpecUtils with Fronts with DirtyEnum {
	override type TagSpec[A] = S[A]

	protected trait S[A] extends TagSpecAbs[A] with Front[A] with DirtyElem
	protected class SI[A: TypeTag](data: IOData[A]) extends Impl[A](data) with S[A]

	implicit val EndTag = new End with S[Null]
	implicit val ByteTag = new SI(ioByte)
	implicit val ShortTag = new SI(ioShort)
	implicit val IntTag = new SI(ioInt)
	implicit val LongTag = new SI(ioLong)
	implicit val FloatTag = new SI(ioFloat)
	implicit val DoubleTag = new SI(ioDouble)
	implicit val BytesTag = new SI(new SeqIO(ioByte))
	implicit val StringTag = new SI(ioString)
	implicit val TagSeqTag = new SI(TagSpecIO)
	implicit val TagMapTag = new SI(new TagMapIO)
}

object Tags19132 extends Tags19132
