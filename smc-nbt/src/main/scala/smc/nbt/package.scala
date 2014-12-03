package smc

import nbt.IO._
import scala.collection.immutable._
import scala.annotation.implicitNotFound
import scala.language.implicitConversions
import scala.reflect.runtime.universe._

package object nbt extends Enum {
	protected override type ElemExtended = NbtSpec[_]

	@implicitNotFound("${T} is not of NBT.")
	sealed trait NbtSpec[T] extends Elem {
		val ttag: TypeTag[T]

		private[nbt] val valueIO: IO[T]
		private[nbt] val nameIO: IO[String]

		final def apply(t: T): Nbt[T] = Nbt(t)(this)
		final def unapply(n: Nbt[_]): Option[T] = n.getOpt[T](this)
	}
	
	def NbtSpec[A](implicit s: NbtSpec[A]): NbtSpec[A] = s

	private[nbt] object NbtSpecIO extends IO[NbtSpec[_]] {
		override val dec: Dec[NbtSpec[_]] = { (o, n) =>
			o.writeByte(getID(n))
		}
		override val enc: Enc[NbtSpec[_]] = { i =>
			getElem(i.readByte())
		}
	}

	trait Nbt[T] {
		val value: T
		val spec: NbtSpec[T]
	}

	implicit def Nbt[A: NbtSpec](v: A): Nbt[A] = {
		new Nbt[A] {
			override val value = v
			override val spec = NbtSpec[A]
			override val toString = s"Nbt($value)"
		}
	}

	object NbtIO extends IO[(String, Nbt[_])] {
		private def decn[A](o: O, m: String, n: Nbt[A]): Unit = {
			NbtSpecIO.dec(o, n.spec)
			n.spec.nameIO.dec(o, m)
			n.spec.valueIO.dec(o, n.value)
		}
		private def encn[A](i: I, s: NbtSpec[A]) = {
			val m = s.nameIO.enc(i)
			val n = s.valueIO.enc(i)
			(m, Nbt(n)(s))
		}
		override val dec: Dec[(String, Nbt[_])] = {
			case (o, (m, n)) => decn(o, m, n)
		}
		override val enc: Enc[(String, Nbt[_])] = {
			case i => encn(i, NbtSpecIO.enc(i))
		}
	}

	trait NbtSeq[T] {
		val value: Seq[T]
		val spec: NbtSpec[T]
	}

	def NbtSeq[A: NbtSpec](v: Seq[A]): NbtSeq[A] = {
		new NbtSeq[A] {
			override val value = v
			override val spec = NbtSpec[A]
			override val toString = s"NbtSeq($value)"
		}
	}

	private object NbtSeqIO extends IO[NbtSeq[_]] {
		private def decn[A](o: O, s: NbtSeq[A]): Unit = {
			NbtSpecIO.dec(o, s.spec)
			o.writeInt(s.value.size)
			s.value.foreach(s.spec.valueIO.dec(o, _))
		}
		private def encn[A](i: I, s: NbtSpec[A]): NbtSeq[A] = {
			val size = i.readInt()
			def body = s.valueIO.enc(i)
			NbtSeq(Seq.fill(size)(body))(s)
		}
		override val dec: Dec[NbtSeq[_]] = (o, n) => decn(o, n)
		override val enc: Enc[NbtSeq[_]] = (i) => encn(i, NbtSpecIO.enc(i))
	}

	type NbtMap = Map[String, Nbt[_]]

	private object NbtMapIO extends IO[NbtMap] {
		override val dec: Dec[NbtMap] = { (o, n) =>
			n.foreach(NbtIO.dec(o, _))
			NbtIO.dec(o, "" -> Nbt(null)(NbtEnd))
		}
		override val enc: Enc[NbtMap] = { i =>
			def body = NbtIO.enc(i)
			Iterator.continually(body).takeWhile(_._2.spec ne NbtEnd).toMap
		}
	}

	private def seqIO[T](p: IO[T]): IO[Seq[T]] = {
		new IO[Seq[T]] {
			override val dec: Dec[Seq[T]] = { (o, n) =>
				o.writeInt(n.size)
				n.foreach(p.dec(o, _))
			}
			override val enc: Enc[Seq[T]] = { i =>
				val size = i.readInt()
				def body = p.enc(i)
				Seq.fill(size)(body)
			}
		}
	}

	implicit val NbtEnd: NbtSpec[Null] = {
		new NbtSpec[Null]  {
			override val ttag = typeTag[Null]
			override val valueIO = io[Null](_ => null, (_, _) => Unit)
			override val nameIO = io[String](_ => "", (_, _) => Unit)
		}
	}

	private def spec[A: TypeTag](v: IO[A], n: IO[String] = StringIO): NbtSpec[A] = {
		new NbtSpec[A] {
			override val ttag = typeTag[A]
			override private[nbt] val nameIO = n
			override private[nbt] val valueIO = v
		}
	}

	implicit val NbtByte   = spec(ByteIO)
	implicit val NbtShort  = spec(ShortIO)
	implicit val NbtInt    = spec(IntIO)
	implicit val NbtLong   = spec(LongIO)
	implicit val NbtFloat  = spec(FloatIO)
	implicit val NbtDouble = spec(DoubleIO)
	implicit val NbtBytes  = spec(seqIO(ByteIO))
	implicit val NbtString = spec(StringIO)
	implicit val NbtSeq    = spec(NbtSeqIO)
	implicit val NbtMap    = spec(NbtMapIO)
	implicit val NbtInts   = spec(seqIO(IntIO))

	implicit class NbtSeqOp(val n: NbtSeq[_]) extends AnyVal {
		def get[U: NbtSpec]: Seq[U] = {
			n.value.asInstanceOf[Seq[U]]
		}
		def getOpt[U: NbtSpec]: Option[Seq[U]] = {
			val tT = n.spec.ttag.tpe
			val tU = NbtSpec[U].ttag.tpe
			if (tT <:< tU) Some(get[U]) else None
		}
	}

	implicit class NbtOp(val n: Nbt[_]) extends AnyVal {
		def get[U: NbtSpec]: U = {
			n.value.asInstanceOf[U]
		}
		def getOpt[U: NbtSpec]: Option[U] = {
			val tT = n.spec.ttag.tpe
			val tU = NbtSpec[U].ttag.tpe
			if (tT <:< tU) Some(get[U]) else None
		}
	}

	implicit class NbtOpSeq(val n: Nbt[_]) extends AnyVal {
		def seq[U: NbtSpec]: Seq[U] = {
			n.get(NbtSeq).get[U]
		}
		def seqOpt[U: NbtSpec]: Option[Seq[U]] = {
			n.getOpt(NbtSeq).flatMap(_.getOpt[U])
		}
	}

	implicit class NbtI(val i: I) extends AnyVal {
		def readNbt(): (String, Nbt[_]) = NbtIO.enc(i)
	}

	implicit class NbtO(val o: O) extends AnyVal {
		def writeNbt(n: (String, Nbt[_])): Unit = NbtIO.dec(o, n)
	}

	implicit class ByteOp(val b: Byte) extends AnyVal {
		def toBool: Boolean = b != 0
	}

	implicit class BoolOp(val b: Boolean) extends AnyVal {
		def toByte: Byte = if (b) 1 else 0
	}
}
