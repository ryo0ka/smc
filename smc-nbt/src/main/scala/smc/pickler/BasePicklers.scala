package smc.pickler

trait BasePicklers {
	val byte: Pickler[Byte]
	val short: Pickler[Short]
	val int: Pickler[Int]
	val long: Pickler[Long]
	val float: Pickler[Float]
	val double: Pickler[Double]
	val string: Pickler[String]
}
