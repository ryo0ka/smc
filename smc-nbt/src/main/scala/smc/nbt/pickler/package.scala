package smc.nbt

package object pickler {
	type BytesI = Iterator[Byte]
	type BytesO = Seq[Byte]
	type Pickle[A] = A => BytesO
	type Unpickle[A] = BytesI => A
}
