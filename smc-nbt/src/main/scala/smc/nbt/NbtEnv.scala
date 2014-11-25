package smc.nbt

import smc.nbt.pickler._, Pickler._
import scala.collection._
import scala.util.control.Exception._

final class NbtEnv(base: BasePicklers) extends ProtectedEnum {

	sealed trait NbtSpec[T] extends ProtectedElem {
		private[nbt] val valp: Pickler[T]
		private[nbt] val namep: Pickler[String]

		final def unapply(n: Nbt[_]): Option[T] = {
			allCatch opt n.get[T](this)
		}

		final def apply(n: T): Nbt[T] = {
			Nbt(n)(this)
		}
	}

	protected override type ProtectedElemType = NbtSpec[_]

	private object NbtSpec extends Pickler[NbtSpec[_]] {
		val id = base.byte

		override def pickle(o: O, n: NbtSpec[_]): Unit = {
			id.pickle(o, getID(n).toByte)
		}
		override def unpickle(i: I): NbtSpec[_] = {
			getElem(id.unpickle(i))
		}
		def apply[A](implicit s: NbtSpec[A]) = s
	}

	sealed trait Nbt[T] {
		val value: T
		private[nbt] val spec: NbtSpec[T]

		final def get[U: NbtSpec]: U = {
			value.asInstanceOf[U]
		}
	}

	implicit def Nbt[A: NbtSpec](v: A): Nbt[A] = {
		new Nbt[A] {
			override val spec = NbtSpec[A]
			override val value = v
			override val toString = s"Nbt($v)"
		}
	}

	object Nbt extends Pickler[(String, Nbt[_])] {
		private def dec[A](o: O, m: String, n: Nbt[A]): Unit = {
			NbtSpec.pickle(o, n.spec)
			n.spec.namep.pickle(o, m)
			n.spec.valp.pickle(o, n.value)
		}
		private def enc[A](i: I, s: NbtSpec[A]) = {
			val n = s.namep.unpickle(i)
			val b = s.valp.unpickle(i)
			(n, Nbt(b)(s))
		}
		override def pickle(o: O, n: (String, Nbt[_])): Unit = {
			dec(o, n._1, n._2)
		}
		override def unpickle(i: I): (String, Nbt[_]) = {
			enc(i, NbtSpec.unpickle(i))
		}
	}

	sealed trait NbtSeq[T] {
		val value: Seq[T]
		private[nbt] val spec: NbtSpec[T]

		final def get[U: NbtSpec]: Seq[U] = {
			value.asInstanceOf[Seq[U]]
		}
	}

	def NbtSeq[A: NbtSpec](v: Seq[A]): NbtSeq[A] = {
		new NbtSeq[A] {
			override val spec = NbtSpec[A]
			override val value = v
			override val toString = s"NbtSeq($v)"
		}
	}

	type NbtMap = Map[String, Nbt[_]]

	object NbtEnd extends NbtSpec[Null] with Nbt[Null] {
		override val value = null: Null
		override private[nbt] val spec = this
		override private[nbt] val valp = new Pickler[Null] {
			override def pickle(o: O, n: Null): Unit = {}
			override def unpickle(i: I): Null = null: Null
		}
		override private[nbt] val namep = new Pickler[String] {
			override def pickle(o: O, n: String): Unit = {}
			override def unpickle(i: I): String = ""
		}
		val named: (String, Nbt[Null]) = ("", this)
	}

	private final class SeqPickler[T](p: Pickler[T]) extends Pickler[Seq[T]] {
		override def pickle(o: O, n: Seq[T]): Unit = {
			base.int.pickle(o, n.size)
			n.foreach(e => p.pickle(o, e))
		}
		override def unpickle(i: I): Seq[T] = {
			val size = base.int.unpickle(i)
			def body = p.unpickle(i)
			Seq.fill(size)(body)
		}
	}

	private object NbtSeqPickler extends Pickler[NbtSeq[_]] {
		def dec[A](o: O, s: NbtSeq[A]): Unit = {
			NbtSpec.pickle(o, s.spec)
			base.int.pickle(o, s.value.size)
			s.value.foreach(e => s.spec.valp.pickle(o, e))
		}
		def enc[A](i: I, s: NbtSpec[A]): NbtSeq[A] = {
			val size = base.int.unpickle(i)
			def body = s.valp.unpickle(i)
			NbtSeq(Seq.fill(size)(body))(s)
		}
		override def pickle(o: O, n: NbtSeq[_]): Unit = {
			dec(o, n)
		}
		override def unpickle(i: I): NbtSeq[_] = {
			enc(i, NbtSpec.unpickle(i))
		}
	}

	private object NbtMapPickler extends Pickler[NbtMap] {
		override def pickle(o: O, n: NbtMap): Unit = {
			n.foreach(Nbt.pickle(o, _))
			Nbt.pickle(o, NbtEnd.named)
		}
		override def unpickle(i: I): NbtMap = {
			def body = Nbt.unpickle(i)
			def notEnd(e: (String, Nbt[_])) = e._2.spec != NbtEnd
			Stream.continually(body).takeWhile(notEnd).toMap
		}
	}

	class NbtSpecAbs[A] private[NbtEnv](v: Pickler[A]) extends NbtSpec[A] {
		override val namep = base.string
		override private[nbt] val valp = v
	}
	implicit object NbtByte extends NbtSpecAbs(base.byte)
	implicit object NbtShort extends NbtSpecAbs(base.short)
	implicit object NbtInt extends NbtSpecAbs(base.int)
	implicit object NbtLong extends NbtSpecAbs(base.long)
	implicit object NbtFloat extends NbtSpecAbs(base.float)
	implicit object NbtDouble extends NbtSpecAbs(base.double)
	implicit object NbtBytes extends NbtSpecAbs(new SeqPickler(base.byte))
	implicit object NbtString extends NbtSpecAbs(base.string)
	implicit object NbtSeq extends NbtSpecAbs(NbtSeqPickler)
	implicit object NbtMap extends NbtSpecAbs(NbtMapPickler)
	implicit object NbtInts extends NbtSpecAbs(new SeqPickler(base.int))
}
