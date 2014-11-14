package smc.nbt

import scala.collection.{mutable => mut}
import scala.reflect.runtime.universe._
import Pickler._

final class Nbt(base: BasePicklers) {
	private val picklers = mut.Buffer[NbtPickler]()

	sealed trait NbtPickler {
		type Value
		final val id = picklers.length.toByte
		picklers :+ this

		implicit val ttag: TypeTag[Value] //for NbtSeq
		val pickle: Pickle[Value]
		val unpickle: Unpickle[NbtagT[Value]]
	}

	type NbtPicklerT[A] = NbtPickler { type Value = A }

	sealed trait Nbtag {
		type Value
		val value: Value
		val pickler: NbtPicklerT[Value]
	}

	type NbtagT[A] = Nbtag { type Value = A }

	def pickle[A](value: A)(implicit p: NbtPicklerT[A]) = {
		base.byte.pickle(p.id) ++ p.pickle(value)
	}

	def unpickle(in: BytesI): Nbtag = {
		val id = base.byte.unpickle(in)
		val pickler = picklers(id)
		pickler.unpickle(in)
	}

	private object NbtEnd extends Nbtag with NbtPickler {
		type Value = Null
		val pickler = this
		val ttag = null
		val value = null
		val pickle: Pickle[Null] = n => null
		val unpickle: Unpickle[NbtEnd.type] = n => null
	}

	sealed abstract class AbsNbtag[A](val pickler: NbtPicklerT[A]) extends Nbtag {
		type Value = A
	}

	sealed abstract class AbsNbtPickler[A: TypeTag, B <: NbtagT[A]](
		pickler: Pickler[A]) extends NbtPickler {
		type Value = A
		val ttag = typeTag[A]
		def apply(v: A): B
		val pickle = pickler.pickle
		val unpickle = pickler.unpickle andThen apply
	}

	case class NbtByte(value: Byte) extends AbsNbtag(NbtByte)
	implicit object NbtByte extends AbsNbtPickler[Byte, NbtByte](base.byte)
	
	case class NbtShort(value: Short) extends AbsNbtag(NbtShort)
	implicit object NbtShort extends AbsNbtPickler[Short, NbtShort](base.short)
	
	case class NbtInt(value: Int) extends AbsNbtag(NbtInt)
	implicit object NbtInt extends AbsNbtPickler[Int, NbtInt](base.int)
	
	case class NbtLong(value: Long) extends AbsNbtag(NbtLong)
	implicit object NbtLong extends AbsNbtPickler[Long, NbtLong](base.long)
	
	case class NbtFloat(value: Float) extends AbsNbtag(NbtFloat)
	implicit object NbtFloat extends AbsNbtPickler[Float, NbtFloat](base.float)
	
	case class NbtDouble(value: Double) extends AbsNbtag(NbtDouble)
	implicit object NbtDouble extends AbsNbtPickler[Double, NbtDouble](base.double)

	sealed abstract class SeqNbtPickelr[A: TypeTag, B <: NbtagT[Seq[A]]](
		pickler: Pickler[A]) extends AbsNbtPickler[Seq[A], B](new Pickler[Seq[A]] {
		val pickle: Pickle[Seq[A]] = { seq =>
			val size = base.int.pickle(seq.length)
			val body = seq.flatMap(pickler.pickle)
			size ++ body
		}
		val unpickle: Unpickle[Seq[A]] = { in =>
			val size = base.int.unpickle(in)
			(0 until size).map(_ => pickler.unpickle(in))
		}
	})

	case class NbtBytes(value: Seq[Byte]) extends AbsNbtag[Seq[Byte]](NbtBytes)
	implicit object NbtBytes extends SeqNbtPickelr[Byte, NbtBytes](base.byte)

	case class NbtString(value: String) extends AbsNbtag(NbtString)
	implicit object NbtString extends AbsNbtPickler[String, NbtString](base.string)

	sealed trait AbsNbtSeq {
		type Elem
		val elems: Seq[Elem]
		val elemPickler: NbtPicklerT[Elem]
		implicit val ttag: TypeTag[Elem] = elemPickler.ttag
		def elemsAs[Target: TypeTag]: Option[Seq[Target]] = {
			val tV = typeOf[Elem]
			val tT = typeOf[Target]
			def eT = elems.asInstanceOf[Seq[Target]]
			if (tV <:< tT) Some(eT) else None
		}
	}

	case class NbtSeq[A: NbtPicklerT](elems: Seq[A]) extends AbsNbtSeq with Nbtag {
		type Elem = A
		type Value = AbsNbtSeq
		val value = this
		val pickler = NbtSeq
		val elemPickler = implicitly[NbtPicklerT[A]]
	}

	implicit object NbtSeq extends NbtPickler {
		type Value = AbsNbtSeq
		val ttag = typeTag[AbsNbtSeq]
		val pickle: Pickle[AbsNbtSeq] = { ns =>
			val id = base.byte.pickle(ns.elemPickler.id)
			val size = base.int.pickle(ns.elems.size)
			val body = ns.elems.flatMap(ns.elemPickler.pickle)
			id ++ size ++ body
		}
		val unpickle: Unpickle[NbtSeq[_]] = { in =>
			val id = base.byte.unpickle(in)
			val epic = picklers(id)
			val size = base.int.unpickle(in)
			val body = (0 until size).map(_ => epic.unpickle(in).value)
			NbtSeq[epic.Value](body)(epic)
		}
	}

	type NbtMapV = String Map Nbtag

	case class NbtMap(value: NbtMapV) extends Nbtag {
		type Value = NbtMapV
		val pickler = NbtMap
	}

	implicit object NbtMap extends NbtPickler {
		type Value = NbtMapV
		val ttag = typeTag[NbtMapV]
		val pickle: Pickle[NbtMapV] = { m =>
			val body = m.flatMap { kv =>
				val (k, v) = kv
				val name = base.string.pickle(k)
				val id = base.byte.pickle(v.pickler.id)
				val body = v.pickler.pickle(v.value)
				name ++ id ++ body
			}
			body.toStream :+ NbtEnd.id
		}
		val unpickle: Unpickle[NbtMap] = { in =>
			new NbtMap(Stream continually {
				val name = base.string.unpickle(in)
				val id = base.byte.unpickle(in)
				val epic = picklers(id)
				val body = epic.unpickle(in)
				(name, body: Nbtag)
			} takeWhile(_._2.pickler != NbtEnd) toMap)
		}
	}

	case class NbtInts(value: Seq[Int]) extends AbsNbtag[Seq[Int]](NbtInts)
	implicit object NbtInts extends SeqNbtPickelr[Int, NbtInts](base.int)
}
