package smc.nbt

import java.io.{InputStream, OutputStream}

package object pickler {
	type I = InputStream
	type O = OutputStream
	type Pickle[A] = (O, A) => Unit
	type Unpickle[A] = I => A
	type BPickle[A] = A => Seq[Byte]
	type BUnpickle[A] = Iterator[Byte] => A
}
