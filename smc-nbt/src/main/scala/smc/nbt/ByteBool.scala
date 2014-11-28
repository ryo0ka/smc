package smc.nbt

object ByteBool {
	type Bool = Boolean

	implicit val toBool: Byte => Bool = _ != 0
	implicit val toByte: Bool => Byte = if (_) 1 else 0
}
