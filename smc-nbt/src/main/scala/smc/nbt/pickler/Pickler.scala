package smc.nbt.pickler

import java.io.{ByteArrayOutputStream => BAOS}

trait Pickler[A] {
	val pickle: Pickle[A]
	val unpickle: Unpickle[A]
}

object Pickler {
	def apply[A](i: Unpickle[A], o: Pickle[A]) = new Pickler[A] {
		override val pickle = o
		override val unpickle = i
	}
}

