package smc.nbt

import scala.collection.mutable

trait ProtectedEnum {
	protected trait ProtectedElem {
		private[ProtectedEnum] val id = elems.length
		elems += this.asInstanceOf[ProtectedElemType]
	}

	protected type ProtectedElemType <: ProtectedElem

	private val elems = mutable.ArrayBuffer[ProtectedElemType]()

	protected def getElem(id: Int): ProtectedElemType = elems(id)
	protected def getID(elem: ProtectedElemType): Int = elem.id
}
