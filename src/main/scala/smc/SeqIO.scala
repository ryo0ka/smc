package smc

import java.io.{DataInput, DataOutput}
import DataIO._

import scala.collection.immutable._

class SeqIO[T](data: IOData[T]) extends IODataAbs[Seq[T]] {
	override def apply(i: DataInput) = {
		IndexedSeq.fill(i.readInt())(i.readData(data))
	}
	override def apply(o: DataOutput, s: Seq[T]) = {
		o.writeInt(s.size)
		s.foreach(o.writeData[T](_)(data))
	}
}
