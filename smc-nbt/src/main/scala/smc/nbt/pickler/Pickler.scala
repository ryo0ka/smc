package smc.nbt.pickler

import java.io.{ByteArrayOutputStream => BAOS}

trait Pickler[A] {
	val pickle: Pickle[A]
	val unpickle: Unpickle[A]
	val bpickle: BPickle[A]
	val bunpickle: BUnpickle[A]
}

object Pickler {
	def apply[A](i: Unpickle[A])(o: Pickle[A]) = new Pickler1[A] {
		override val pickle = o
		override val unpickle = i
	}
	def apply[A](i: BUnpickle[A])(o: BPickle[A]) = new Pickler2[A] {
		override val bpickle = o
		override val bunpickle = i
	}
}

trait Pickler1[A] extends Pickler[A] {
	override val bpickle: BPickle[A] = { n =>
		new BAOS() {
			pickle(this, n)
			val b: Seq[Byte] = buf
		}.b
	}
	override val bunpickle: BUnpickle[A] = { i =>
		unpickle(new I {
			def read = i.next()
		})
	}
}

trait Pickler2[A] extends Pickler[A] {
	override val pickle: Pickle[A] = { (o, n) =>
		bpickle(n).foreach(b => o.write(b))
	}
	override val unpickle: Unpickle[A] = { i =>
		val iter = Iterator
			.continually(i.read())
			.takeWhile(_ != -1)
			.map(_.toByte)
		bunpickle(iter)
	}
}
