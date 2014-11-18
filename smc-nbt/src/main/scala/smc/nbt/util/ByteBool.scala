package smc.nbt.util

object ByteBool {
	type Bool = Boolean

	val toBool: Byte => Bool = _ != 0
	val toByte: Bool => Byte = if (_) 1 else 0

	implicit class ByteOp(val b: Byte) extends AnyVal {
		def bool: Bool = toBool(b)
	}

	implicit class BoolOp(val b: Bool) extends AnyVal {
		def byte: Byte = toByte(b)
	}
}
