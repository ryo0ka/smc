package smc

import java.io.{DataInput, DataOutput}
import scala.language.implicitConversions

object DataIO {
	type InputData[+T] = DataInput => T
	type OutputData[-T] = (DataOutput, T) => Unit

	implicit class DataInputW(val i: DataInput) extends AnyVal {
		def readData[A: InputData]: A = implicitly[InputData[A]].apply(i)
	}

	implicit class DataOutputW(val o: DataOutput) extends AnyVal {
		def writeData[A: OutputData](a: A): Unit = implicitly[OutputData[A]].apply(o, a)
	}

	type IOData[A] = InputData[A] with OutputData[A]

	trait IODataAbs[A] extends InputData[A] with OutputData[A]

	def ioData[A](id: InputData[A], od: OutputData[A]): IOData[A] = new IODataAbs[A] {
		override def apply(i: DataInput) = id(i)
		override def apply(o: DataOutput, a: A) = od(o, a)
	}

	implicit val ioByte = ioData[Byte](_.readByte(), _.writeByte(_))
	implicit val ioBool = ioData[Boolean](_.readBoolean(), _.writeBoolean(_))
	implicit val ioChar = ioData[Char](_.readChar(), _.writeChar(_))
	implicit val ioShort = ioData[Short](_.readShort(), _.writeShort(_))
	implicit val ioInt = ioData[Int](_.readInt(), _.writeInt(_))
	implicit val ioLong = ioData[Long](_.readLong(), _.writeLong(_))
	implicit val ioFloat = ioData[Float](_.readFloat(), _.writeFloat(_))
	implicit val ioDouble = ioData[Double](_.readDouble(), _.writeDouble(_))
	implicit val ioString = ioData[String](_.readUTF(), _.writeUTF(_))

}
