package smc.nbt

import scala.collection.mutable.ArrayBuffer

trait Enum {
	protected trait Elem {
		private[Enum] val id = elems.length
		elems += this.asInstanceOf[ElemExtended]
	}

	protected type ElemExtended <: Elem

	private val elems = ArrayBuffer[ElemExtended]()

	def getElem(id: Int): ElemExtended = elems(id)
	def getID(elem: ElemExtended): Int = elem.id
}
