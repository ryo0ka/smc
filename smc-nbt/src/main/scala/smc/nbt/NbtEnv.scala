package smc.nbt

import pickler._
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

	private object NbtSpec extends Pickler[NbtSpecN] {
		val id = base.byte
		override val pickle: Pickle[NbtSpecN] = { s =>
			id.pickle(getID(s).toByte)
		}
		override val unpickle: Unpickle[NbtSpecN] = { in =>
			getElem(id.unpickle(in))
		}
		def apply[A](implicit s: NbtSpec[A]): NbtSpec[A] = s
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

	object Nbt extends Pickler[NbtN] {
		override val pickle: Pickle[NbtN] = { n =>
			NbtSpec.pickle(n.spec) ++ n.spec.pickler.pickle(n.value)
		}
		override val unpickle: Unpickle[NbtN] = { in =>
			val spec = NbtSpec.unpickle(in)
			val value = spec.pickler.unpickle(in)
			Nbt(value)(spec): NbtN
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
	
	private object NbtEndPickler extends Pickler[Null] {
		override val pickle: Pickle[Null] = n => Seq()
		override val unpickle: Unpickle[Null] = in => null
	}

	private final class SeqPickler[T](p: Pickler[T]) extends Pickler[Seq[T]] {
		override val pickle: Pickle[Seq[T]] = { s =>
			val size = base.int.pickle(s.size)
			val body = s.flatMap(p.pickle)
			size ++ body
		}
		override val unpickle: Unpickle[Seq[T]] = { in =>
			val size = base.int.unpickle(in)
			Seq.fill(size)(p.unpickle(in))
		}
	}

	private object NbtSeqPickler extends Pickler[NbtSeqN] {
		override val pickle: Pickle[NbtSeqN] = { s =>
			val id = NbtSpec.pickle(s.spec)
			val size = base.int.pickle(s.value.size)
			val body = s.value.flatMap(s.spec.pickler.pickle)
			id ++ size ++ body
		}
		override val unpickle: Unpickle[NbtSeqN] = { in =>
			val spec = NbtSpec.unpickle(in)
			val size = base.int.unpickle(in)
			def body = spec.pickler.unpickle(in)
			NbtSeq(Seq.fill(size)(body))(spec)
		}
	}

	private object NbtMapPickler extends Pickler[NbtMap] {
		private type Entry = (String, NbtN)

		object EntryPickler extends Pickler[Entry] {
			override val pickle: Pickle[Entry] = { e =>
				val (name, n) = e
				val bname = base.string.pickle(name)
				val bnbt = n.spec.pickler.pickle(n.value)
				bname ++ bnbt
			}
			override val unpickle: Unpickle[Entry] = { in =>
				val name = base.string.unpickle(in)
				val nbt = Nbt.unpickle(in)
				(name, nbt)
			}
		}

		override val pickle: Pickle[NbtMap] = { m =>
			val body = m.flatMap(EntryPickler.pickle)
			val end = Nbt.pickle(Nbt(null)(NbtEnd))
			body.toStream ++ end
		}
		override val unpickle: Unpickle[NbtMap] = { in =>
			Stream(EntryPickler.unpickle(in))
				.takeWhile(_._2.spec != NbtEnd).toMap
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
