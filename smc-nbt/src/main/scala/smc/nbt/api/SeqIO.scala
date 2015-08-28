package smc.nbt.api

import java.io.{DataInput, DataOutput}

import dataio._

import scala.collection.immutable._

class SeqIO[T](data: IOData[T]) extends IODataAbs[Seq[T]] {
	override def apply(i: DataInput): Seq[T] = {
		IndexedSeq.fill(i.readInt())(i.readData(data))
	}
	override def apply(o: DataOutput, s: Seq[T]): Unit = {
		o.writeInt(s.size)
		s.foreach(o.writeData[T](_)(data))
	}
}
