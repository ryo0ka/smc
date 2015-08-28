package smc.nbt.test.nbt_old

import java.io._

object IO {
	type I = DataInput
	type O = DataOutput

	type Enc[+A] = I => A
	type Dec[-A] = (O, A) => Unit

	trait IO[T] {
		val enc: Enc[T]
		val dec: Dec[T]
	}

	def io[A](e: Enc[A], d: Dec[A]): IO[A] = {
		new IO[A] {
			override val enc = e
			override val dec = d
		}
	}
	
	val ByteIO = io[Byte](_.readByte(), _.writeByte(_))
	val ShortIO = io[Short](_.readShort(), _.writeShort(_))
	val IntIO = io[Int](_.readInt(), _.writeInt(_))
	val LongIO = io[Long](_.readLong(), _.writeLong(_))
	val FloatIO = io[Float](_.readFloat(), _.writeFloat(_))
	val DoubleIO = io[Double](_.readDouble(), _.writeDouble(_))
	val StringIO = io[String](_.readUTF(), _.writeUTF(_))
}
