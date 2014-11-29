package smc

import nbt.IO._
import scala.collection._
import scala.annotation.implicitNotFound
import scala.language.implicitConversions

package object nbt extends Enum {

	@implicitNotFound("${T} is not of NBT. You may want to import `smc.nbt._` and try again.")
	sealed trait NbtSpec[T] extends Elem {
		private[nbt] val valp: IO[T]
		private[nbt] val namep: IO[String]

		final def apply(v: T) = Nbt[T](v)(this)
	}

	protected override type ElemExtended = NbtSpec[_]

	private object NbtSpec extends IO[NbtSpec[_]] {
		override val dec: Dec[NbtSpec[_]] = { (o, n) =>
			o.writeByte(getID(n))
		}
		override val enc: Enc[NbtSpec[_]] = { i =>
			getElem(i.readByte())
		}

		def apply[A](implicit s: NbtSpec[A]) = s
	}

	sealed trait Nbt[T] {
		val value: T
		private[nbt] val spec: NbtSpec[T]
	}

	implicit def Nbt[A: NbtSpec](v: A): Nbt[A] = {
		new Nbt[A] {
			override val spec = NbtSpec[A]
			override val value = v
			override val toString = s"Nbt($v)"
		}
	}

	implicit final class NbtOp(val n: Nbt[_]) extends AnyVal {
		def get[U: NbtSpec]: U = {
			n.value.asInstanceOf[U]
		}

		def getSeq[U: NbtSpec]: Seq[U] = {
			get(NbtSeq).get[U]
		}
	}

	object Nbt extends IO[(String, Nbt[_])] {
		override val dec: Dec[(String, Nbt[_])] = {
			case (o, (m, n)) => decn(o, m, n)
		}
		override val enc: Enc[(String, Nbt[_])] = {
			case i => encn(i, NbtSpec.enc(i))
		}
		private def decn[A](o: O, m: String, n: Nbt[A]): Unit = {
			NbtSpec.dec(o, n.spec)
			n.spec.namep.dec(o, m)
			n.spec.valp.dec(o, n.value)
		}
		private def encn[A](i: I, s: NbtSpec[A]) = {
			val n = s.namep.enc(i)
			val b = s.valp.enc(i)
			(n, Nbt(b)(s))
		}
	}

	sealed trait NbtSeq[T] {
		val value: Seq[T]
		private[nbt] val spec: NbtSpec[T]
	}

	def NbtSeq[A: NbtSpec](v: Seq[A]): NbtSeq[A] = {
		new NbtSeq[A] {
			override val spec = NbtSpec[A]
			override val value = v
			override val toString = s"NbtSeq($v)"
		}
	}

	implicit final class NbtSeqOp(val n: NbtSeq[_]) extends AnyVal {
		def get[U: NbtSpec]: Seq[U] = {
			n.value.asInstanceOf[Seq[U]]
		}
	}

	type NbtMap = Map[String, Nbt[_]]

	object NbtEnd extends NbtSpec[Null] with Nbt[Null] {
		override val value = null: Null
		override private[nbt] val spec = this
		override private[nbt] val valp = io[Null](_ => null, (_, _) => Unit)
		override private[nbt] val namep = io[String](_ => "", (_, _) => Unit)
		val entry: (String, Nbt[Null]) = ("", this)
	}

	private final case class SeqIO[T](p: IO[T]) extends IO[Seq[T]] {
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

	private object NbtSeqIO extends IO[NbtSeq[_]] {
		def decn[A](o: O, s: NbtSeq[A]): Unit = {
			NbtSpec.dec(o, s.spec)
			o.writeInt(s.value.size)
			s.value.foreach(s.spec.valp.dec(o, _))
		}
		def encn[A](i: I, s: NbtSpec[A]): NbtSeq[A] = {
			val size = i.readInt()
			def body = s.valp.enc(i)
			NbtSeq(Seq.fill(size)(body))(s)
		}
		override val dec: Dec[NbtSeq[_]] = { (o, n) => decn(o, n) }
		override val enc: Enc[NbtSeq[_]] = { i => encn(i, NbtSpec.enc(i)) }
	}

	private object NbtMapIO extends IO[NbtMap] {
		override val dec: Dec[NbtMap] = { (o, n) =>
			n.foreach(Nbt.dec(o, _))
			Nbt.dec(o, NbtEnd.entry)
		}
		override val enc: Enc[NbtMap] = { i =>
			def body = Nbt.enc(i)
			def notEnd(e: (String, Nbt[_])) = e._2.spec != NbtEnd
			Stream.continually(body).takeWhile(notEnd).toMap
		}
	}

	private def spec[A](v: IO[A]): NbtSpec[A] = {
		new NbtSpec[A] {
			override private[nbt] val namep = StringIO
			override private[nbt] val valp = v
		}
	}

	implicit val NbtByte   = spec(ByteIO)
	implicit val NbtShort  = spec(ShortIO)
	implicit val NbtInt    = spec(IntIO)
	implicit val NbtLong   = spec(LongIO)
	implicit val NbtFloat  = spec(FloatIO)
	implicit val NbtDouble = spec(DoubleIO)
	implicit val NbtBytes  = spec(SeqIO(ByteIO))
	implicit val NbtString = spec(StringIO)
	implicit val NbtSeq    = spec(NbtSeqIO)
	implicit val NbtMap    = spec(NbtMapIO)
	implicit val NbtInts   = spec(SeqIO(IntIO))

	implicit class NbtI(val i: I) extends AnyVal {
		def readNbt(): (String, Nbt[_]) = Nbt.enc(i)
	}

	implicit class NbtO(val o: O) extends AnyVal {
		def writeNbt(n: (String, Nbt[_])): Unit = Nbt.dec(o, n)
	}
}
