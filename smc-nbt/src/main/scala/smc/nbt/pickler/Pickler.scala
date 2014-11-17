package smc.nbt.pickler

trait Pickler[A] {
	val pickle: Pickle[A]
	val unpickle: Unpickle[A]
}
