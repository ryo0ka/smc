package smc.nbt

import scala.reflect.runtime.universe._

trait TypeTagged {
	type T
	
	val value: T
	val ttag: TypeTag[T]

	def valueAs[U: TypeTag]: Option[U] = {
		val tT = typeOf[T](ttag)
		val uT = typeOf[U]
		def tU = value.asInstanceOf[U]
		if (tT <:< uT) Some(tU) else None
	}
}
