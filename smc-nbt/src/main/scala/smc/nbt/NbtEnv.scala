package smc.nbt

import smc.pickler._
import scala.collection.immutable._
import scala.reflect.runtime.universe._

final class NbtEnv(base: BasePicklers) extends StrictEnum {

	sealed trait NbtSpecN extends StrictElem {
		type T
		val ttag: TypeTag[T]
		private[nbt] val pickler: Pickler[T]
	}

	type NbtSpec[A] = NbtSpecN { type T = A }

	protected override type Elem = NbtSpecN

	private object NbtSpec extends Pickler1[NbtSpecN] {
		val id = base.byte
		override val pickle: Pickle[NbtSpecN] = { (o, s) =>
			id.pickle(o, getID(s).toByte)
		}
		override val unpickle: Unpickle[NbtSpecN] = { in =>
			getElem(id.unpickle(in))
		}
		private[nbt] def apply[A](implicit s: NbtSpec[A]): NbtSpec[A] = s
	}

	sealed trait NbtN extends TypeTagged {
		val spec: NbtSpec[T]

		final def as[U: NbtSpec]: Option[U] = {
			implicit val t = NbtSpec[U].ttag
			valueAs[U]
		}
		final def apply[U: NbtSpec]: U = as[U].get
	}

	type Nbt[A] = NbtN { type T = A }

	implicit def Nbt[A: NbtSpec](v: A): Nbt[A] = {
		new NbtN {
			override type T = A
			override val spec = NbtSpec[A]
			override val ttag = spec.ttag
			override val value = v
		}
	}

	object Nbt extends Pickler1[NbtN] {
		override val pickle: Pickle[NbtN] = { (o, n) =>
			NbtSpec.pickle(o, n.spec)
			n.spec.pickler.pickle(o, n.value)
		}
		override val unpickle: Unpickle[NbtN] = { in =>
			val spec = NbtSpec.unpickle(in)
			val body = spec.pickler.unpickle(in)
			Nbt(body)(spec): NbtN
		}
	}

	sealed trait NbtSeqN extends TypeTagged {
		type E
		override type T = Seq[E]
		val spec: NbtSpec[E]
		implicit val ettag = spec.ttag
		override val ttag = typeTag[T]

		final def as[U: NbtSpec]: Option[Seq[U]] = {
			implicit val t = NbtSpec[U].ttag
			valueAs[Seq[U]]
		}
		final def apply[U: NbtSpec]: Seq[U] = as[U].get
	}

	type NbtSeq[A] = NbtSeqN { type E = A }

	def NbtSeq[A: NbtSpec](v: Seq[A]): NbtSeq[A] = {
		new NbtSeqN {
			override type E = A
			override val spec = NbtSpec[A]
			override val value = v
		}
	}

	type NbtMap = Map[String, NbtN]
	
	private object NbtEndPickler extends Pickler1[Null] {
		override val pickle: Pickle[Null] = (o, n) => Unit
		override val unpickle: Unpickle[Null] = in => null
	}

	private final class SeqPickler[T](p: Pickler[T]) extends Pickler1[Seq[T]] {
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

	private object NbtSeqPickler extends Pickler1[NbtSeqN] {
		override val pickle: Pickle[NbtSeqN] = { (o, s) =>
			NbtSpec.pickle(o, s.spec)
			base.int.pickle(o, s.value.size)
			s.value.foreach(e => s.spec.pickler.pickle(o, e))
		}
		override val unpickle: Unpickle[NbtSeqN] = { in =>
			val spec = NbtSpec.unpickle(in)
			val size = base.int.unpickle(in)
			def body = spec.pickler.unpickle(in)
			NbtSeq(Seq.fill(size)(body))(spec)
		}
	}

	private object NbtMapPickler extends Pickler1[NbtMap] {
		type Entry = (String, NbtN)

		object EntryPickler extends Pickler1[Entry] {
			val NamePickler = base.string

			override val pickle: Pickle[Entry] = { (o, e) =>
				val (name, n) = e
				NbtSpec.pickle(o, n.spec)
				NamePickler.pickle(o, name)
				n.spec.pickler.pickle(o, n.value)
			}
			override val unpickle: Unpickle[Entry] = { in =>
				NbtSpec.unpickle(in) match {
					case end: NbtEnd.type => null
					case spec =>
						val name = NamePickler.unpickle(in)
						val value = spec.pickler.unpickle(in)
						val nbt = Nbt(value)(spec)
						(name, nbt)
				}
			}
		}

		override val pickle: Pickle[NbtMap] = { (o, m) =>
			m.foreach(e => EntryPickler.pickle(o, e))
			Nbt.pickle(o, Nbt(null: Null)(NbtEnd))
		}
		override val unpickle: Unpickle[NbtMap] = { in =>
			def body = EntryPickler.unpickle(in)
			def notEnd(e: Entry) = e != null
			Stream.continually(body).takeWhile(notEnd).toMap
		}
	}

	class NbtSpecAbs[A: TypeTag] private[NbtEnv](
		private[nbt] override val pickler: Pickler[A]) extends NbtSpecN {
		override type T = A
		override val ttag = typeTag[A]
	}

	private object NbtEnd extends NbtSpecAbs(NbtEndPickler)
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
