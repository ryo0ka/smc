package smc.nbt.api

import java.io.{DataInput, DataOutput}

import dataio._

import scala.collection.immutable._

trait TagSeqs { this: Tags =>
	case class TagSeq[T](seq: Seq[T])(implicit val spec: TagSpec[T]) {
		final def get[A: TagSpec]: Seq[A] = {
			seq.asInstanceOf[Seq[A]]
		}
		final def getOpt[A: TagSpec]: Option[Seq[A]] = {
			val tA = spec.ttag.tpe
			val tB = implicitly[TagSpec[A]].ttag.tpe
			if (tA <:< tB) Some(get[A]) else None
		}
	}

	protected object TagSeqIO extends IODataAbs[TagSeq[_]] {
		override def apply(i: DataInput) = read(i, i.readData(TagSpecIO))
		override def apply(o: DataOutput, s: TagSeq[_]) = write(o, s)

		private def read[A](i: DataInput, s: TagSpec[A]): TagSeq[A] = {
			val size = i.readInt()
			def data = i.readData(s.data)
			TagSeq(IndexedSeq.fill(size)(data))(s)
		}

		private def write[A](o: DataOutput, s: TagSeq[A]): Unit = {
			o.writeData(s.spec)
			o.writeInt(s.seq.size)
			s.seq.foreach(o.writeData(_)(s.spec.data))
		}
	}

	implicit class SeqTagOp(t: Tag[TagSeq[_]])(implicit s: TagSpec[TagSeq[_]]) {
		def seq[A: TagSpec]: Seq[A] = t.get(s).get[A]
		def seqOpt[A: TagSpec]: Option[Seq[A]] = t.getOpt(s).flatMap(_.getOpt[A])
	}
}
