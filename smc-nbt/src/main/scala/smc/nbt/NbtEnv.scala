package smc.nbt

import smc.nbt.pickler._
import scala.collection.immutable._
import scala.util.control.Exception._

final class NbtEnv(base: BasePicklers) extends ProtectedEnum {

	sealed trait NbtSpecN extends ProtectedElem {
		type T
		private[nbt] val value: Pickler[T]
		private[nbt] val name: Pickler[String]

		final def unapply(n: NbtN): Option[T] = {
			allCatch opt n.get[T](this)
		}

		final def apply(n: T): Nbt[T] = {
			Nbt(n)(this)
		}
	}

	type NbtSpec[A] = NbtSpecN { type T = A }

	protected override type ProtectedElemType = NbtSpecN

	private object NbtSpec extends Pickler[NbtSpecN] {
		val id = base.byte

		override val pickle: Pickle[NbtSpecN] = { (o, s) =>
			id.pickle(o, getID(s).toByte)
		}
		override val unpickle: Unpickle[NbtSpecN] = { in =>
			getElem(id.unpickle(in))
		}

		private[nbt] def apply[A](implicit s: NbtSpec[A]) = s
	}

	sealed trait NbtN {
		type T
		val value: T
		val spec: NbtSpec[T]

		final def get[U: NbtSpec]: U = {
			value.asInstanceOf[U]
		}
	}

	type Nbt[A] = NbtN { type T = A }

	implicit def Nbt[A: NbtSpec](v: A): Nbt[A] = {
		new NbtN {
			override type T = A
			override val spec = NbtSpec[A]
			override val value = v
			override val toString = s"Nbt($v)"
		}
	}

	object Nbt extends Pickler[(String, NbtN)] {
		override val pickle: Pickle[(String, NbtN)] = { (o, n) =>
			val (name, nbt) = n
			NbtSpec.pickle(o, nbt.spec)
			nbt.spec.name.pickle(o, name)
			nbt.spec.value.pickle(o, nbt.value)
		}
		override val unpickle: Unpickle[(String, NbtN)] = { in =>
			val spec = NbtSpec.unpickle(in)
			val name = spec.name.unpickle(in)
			val body = spec.value.unpickle(in)
			(name, Nbt(body)(spec): NbtN)
		}
	}

	sealed trait NbtSeqN {
		type T
		val value: Seq[T]
		val spec: NbtSpec[T]

		final def get[U: NbtSpec]: Seq[U] = {
			value.asInstanceOf[Seq[U]]
		}
	}

	type NbtSeq[A] = NbtSeqN { type T = A }

	def NbtSeq[A: NbtSpec](v: Seq[A]): NbtSeq[A] = {
		new NbtSeqN {
			override type T = A
			override val spec = NbtSpec[A]
			override val value = v
			override val toString = s"NbtSeq($v)"
		}
	}

	type NbtMap = Map[String, NbtN]
	
	private object NbtEndPickler extends Pickler[Null] {
		override val pickle: Pickle[Null] = (o, n) => Unit
		override val unpickle: Unpickle[Null] = in => null: Null
	}

	private final class SeqPickler[T](p: Pickler[T]) extends Pickler[Seq[T]] {
		override val pickle: Pickle[Seq[T]] = { (o, s) =>
			base.int.pickle(o, s.size)
			s.foreach(e => p.pickle(o, e))
		}
		override val unpickle: Unpickle[Seq[T]] = { in =>
			val size = base.int.unpickle(in)
			def body = p.unpickle(in)
			Seq.fill(size)(body)
		}
	}

	private object NbtSeqPickler extends Pickler[NbtSeqN] {
		override val pickle: Pickle[NbtSeqN] = { (o, s) =>
			NbtSpec.pickle(o, s.spec)
			base.int.pickle(o, s.value.size)
			s.value.foreach(e => s.spec.value.pickle(o, e))
		}
		override val unpickle: Unpickle[NbtSeqN] = { in =>
			val spec = NbtSpec.unpickle(in)
			val size = base.int.unpickle(in)
			def body = spec.value.unpickle(in)
			NbtSeq(Seq.fill(size)(body))(spec): NbtSeqN
		}
	}

	private object NbtMapPickler extends Pickler[NbtMap] {
		val end = ("", Nbt(null: Null)(NbtEnd))

		override val pickle: Pickle[NbtMap] = { (o, m) =>
			m.foreach(e => Nbt.pickle(o, e))
			Nbt.pickle(o, end)
		}
		override val unpickle: Unpickle[NbtMap] = { in =>
			def body = Nbt.unpickle(in)
			def notEnd(e: (String, NbtN)) = e._2.spec != NbtEnd
			Stream.continually(body).takeWhile(notEnd).toMap
		}
	}

	class NbtSpecAbs[A] private[NbtEnv](v: Pickler[A]) extends NbtSpecN {
		override type T = A
		override private[nbt] val value = v
		override val name = base.string
	}

	implicit object NbtEnd extends NbtSpecAbs(NbtEndPickler) {
		override val name = new Pickler[String] {
			override val pickle: Pickle[String] = (o, n) => Unit
			override val unpickle: Unpickle[String] = i => ""
		}
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
