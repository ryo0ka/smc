package smc.nbt.pickler

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

	def apply[A](enc: Unpickle[A], dec: Pickle[A]) = new Pickler[A] {
		override def pickle(o: O, n: A): Unit = dec(o, n)
		override def unpickle(i: I): A = enc(i)
	}
}
