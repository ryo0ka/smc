package smc.nbt.pickler

trait BasePicklers {
	val byte: Pickler[Byte]
	val short: Pickler[Short]
	val int: Pickler[Int]
	val long: Pickler[Long]
	val float: Pickler[Float]
	val double: Pickler[Double]
	val string: Pickler[String]
}

object BasePicklers extends BasePicklers {
	import java.io.{DataInputStream => DI, DataOutputStream => DO}

	implicit class DIOP(val s: I) extends AnyVal {
		def d: DI = s match {
			case n: DI => n
			case n => new DI(n)
		}
	}

	implicit class DOOP(val s: O) extends AnyVal {
		def d: DO = s match {
			case n: DO => n
			case n => new DO(n)
		}
	}

	override val byte = Pickler((_: I).d.readByte())(_.d.writeByte(_))
	override val short = Pickler((_: I).d.readShort())(_.d.writeShort(_))
	override val int = Pickler((_: I).d.readInt())(_.d.writeInt(_))
	override val long = Pickler((_: I).d.readLong())(_.d.writeLong(_))
	override val float = Pickler((_: I).d.readFloat())(_.d.writeFloat(_))
	override val double = Pickler((_: I).d.readDouble())(_.d.writeDouble(_))
	override val string = Pickler((_: I).d.readUTF())(_.d.writeUTF(_))
}