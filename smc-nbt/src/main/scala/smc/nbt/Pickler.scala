package smc.nbt

import java.io.{InputStream, OutputStream}
import Pickler._

trait Pickler[A] {
	def pickle(o: O, n: A): Unit
	def unpickle(i: I): A
}

object Pickler {
	type I = InputStream
	type O = OutputStream

	type Pickle[-A] = (O, A) => Unit
	type Unpickle[+A] = I => A

	class AbstractPickler[A](e: Unpickle[A], d: Pickle[A]) extends Pickler[A] {
		override def pickle(o: O, n: A): Unit = d(o, n)
		override def unpickle(i: I): A = e(i)
	}

	def apply[A](e: Unpickle[A], d: Pickle[A]): Pickler[A] = {
		new AbstractPickler[A](e, d)
	}
}