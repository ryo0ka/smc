package smc

import nbt.IO._
import scala.collection._
import scala.annotation.implicitNotFound
import scala.language.implicitConversions

package object nbt extends Enum {

	@implicitNotFound("${T} is not of NBT. You may wish to import `smc.nbt._` and try again.")
	sealed trait NbtSpec[T] extends Elem {
		private[nbt] val value: IO[T]
		private[nbt] val name: IO[String]

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

	final case class Nbt[T](value: T)(implicit val spec: NbtSpec[T])

	implicit def nbt[A: NbtSpec](v: A): Nbt[A] = Nbt[A](v)

	object Nbt extends IO[(String, Nbt[_])] {
		private def decn[A](o: O, m: String, n: Nbt[A]): Unit = {
			NbtSpec.dec(o, n.spec)
			n.spec.name.dec(o, m)
			n.spec.value.dec(o, n.value)
		}
		private def encn[A](i: I, s: NbtSpec[A]) = {
			val n = s.name.enc(i)
			val b = s.value.enc(i)
			(n, Nbt(b)(s))
		}
		override val dec: Dec[(String, Nbt[_])] = {
			case (o, (m, n)) => decn(o, m, n)
		}
		override val enc: Enc[(String, Nbt[_])] = {
			case i => encn(i, NbtSpec.enc(i))
		}
	}

	final case class NbtSeq[T](value: Seq[T])(implicit val spec: NbtSpec[T])

	type NbtMap = Map[String, Nbt[_]]

	object NbtEnd extends NbtSpec[Null] {
		override private[nbt] val value = io[Null](_ => null, (_, _) => Unit)
		override private[nbt] val name = io[String](_ => "", (_, _) => Unit)
		val nbt: Nbt[Null] = this(null: Null)
		val entry: (String, Nbt[Null]) = ("", nbt)
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
		private def decn[A](o: O, s: NbtSeq[A]): Unit = {
			NbtSpec.dec(o, s.spec)
			o.writeInt(s.value.size)
			s.value.foreach(s.spec.value.dec(o, _))
		}
		private def encn[A](i: I, s: NbtSpec[A]): NbtSeq[A] = {
			val size = i.readInt()
			def body = s.value.enc(i)
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
		private def notEnd(e: (String, Nbt[_])): Boolean = {
			e._2.spec != NbtEnd
		}
		override val enc: Enc[NbtMap] = { i =>
			def body = Nbt.enc(i)
			Iterator.continually(body).takeWhile(notEnd).toMap
		}
	}

	private def spec[A](v: IO[A]): NbtSpec[A] = {
		new NbtSpec[A] {
			override private[nbt] val name = StringIO
			override private[nbt] val value = v
		}
	}

	implicit val NbtsByte   = spec(ByteIO)
	implicit val NbtsShort  = spec(ShortIO)
	implicit val NbtsInt    = spec(IntIO)
	implicit val NbtsLong   = spec(LongIO)
	implicit val NbtsFloat  = spec(FloatIO)
	implicit val NbtsDouble = spec(DoubleIO)
	implicit val NbtsBytes  = spec(SeqIO(ByteIO))
	implicit val NbtsString = spec(StringIO)
	implicit val NbtsSeq    = spec(NbtSeqIO)
	implicit val NbtsMap    = spec(NbtMapIO)
	implicit val NbtsInts   = spec(SeqIO(IntIO))

	implicit class NbtI(val i: I) extends AnyVal {
		def readNbt(): (String, Nbt[_]) = Nbt.enc(i)
	}

	implicit class NbtO(val o: O) extends AnyVal {
		def writeNbt(n: (String, Nbt[_])): Unit = Nbt.dec(o, n)
	}

	implicit final class NbtSeqOp(val n: NbtSeq[_]) extends AnyVal {
		def get[U: NbtSpec]: Seq[U] = n.value.asInstanceOf[Seq[U]]
	}

	implicit final class NbtOp(val n: Nbt[_]) extends AnyVal {
		def get[U: NbtSpec]: U = n.value.asInstanceOf[U]
		def getSeq[U: NbtSpec]: Seq[U] = get(NbtsSeq).get[U]
	}

	implicit class ByteOp(val b: Byte) extends AnyVal {
		def toBool: Boolean = b != 0
	}

	implicit class BoolOp(val b: Boolean) extends AnyVal {
		def toByte: Byte = if (b) 1 else 0
	}
}
