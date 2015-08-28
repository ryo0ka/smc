package smc.nbt.api

trait Enum {
	protected type Elem
	protected def element(ordinal: Int): Elem
	protected def ordinal(element: Elem): Int
}

trait DirtyEnum extends Enum {
	protected override type Elem <: DirtyElem
	protected override def element(ordinal: Int) = values(ordinal)
	protected override def ordinal(element: Elem) = element.ordinal

	protected trait DirtyElem {
		val ordinal = values.size
		values += this.asInstanceOf[Elem] //Wow XD
	}

	private val values = collection.mutable.Buffer[Elem]()
}
