package smc.nbt

import scala.collection.mutable

trait StrictEnum {
	protected trait StrictElem {
		private[StrictEnum] val id = elems.length
		elems += this.asInstanceOf[Elem]
	}

	protected type Elem <: StrictElem

	private val elems = mutable.Buffer[Elem]()

	protected def getElem(id: Int): Elem = elems(id)
	protected def getID(elem: Elem): Int = elem.id
}
