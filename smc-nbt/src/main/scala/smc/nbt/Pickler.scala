package smc.nbt

trait Pickler[A] {
	import Pickler._

	val pickle: Pickle[A]
	val unpickle: Unpickle[A]
}

object Pickler {
	type BytesI = Iterator[Byte]
	type BytesO = Seq[Byte]
	type Pickle[A] = A => BytesO
	type Unpickle[A] = BytesI => A

	trait BasePicklers {
		val byte: Pickler[Byte]
		val short: Pickler[Short]
		val int: Pickler[Int]
		val long: Pickler[Long]
		val float: Pickler[Float]
		val double: Pickler[Double]
		val string: Pickler[String]
	}
}
