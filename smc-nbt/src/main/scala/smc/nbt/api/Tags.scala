package smc.nbt.api

import dataio._
import java.io.{DataInput, DataOutput}
import scala.language.{higherKinds, implicitConversions}
import scala.reflect.runtime.universe._

trait Tags { this: Enum =>
	protected override type Elem = TagSpec[_]

	type TagSpec[A] <: TagSpecAbs[A]

	protected trait TagSpecAbs[T] {
		val data: IOData[T]
		val name: IOData[String]
		val ttag: TypeTag[T]
	}

	protected implicit object TagSpecIO extends IODataAbs[TagSpec[_]] {
		override def apply(i: DataInput) = element(i.readByte())
		override def apply(o: DataOutput, n: TagSpec[_]) = o.writeByte(ordinal(n))
	}

	case class Tag[T](value: T)(implicit val spec: TagSpec[T]) {
		final def get[A: TagSpec]: A = {
			value.asInstanceOf[A]
		}
		final def getOpt[A: TagSpec]: Option[A] = {
			val tA = spec.ttag.tpe
			val tB = implicitly[TagSpec[A]].ttag.tpe
			if (tA <:< tB) Some(get[A]) else None
		}
	}

	type Named[A] = (String, A)

	implicit object NamedTagIO extends IODataAbs[Named[Tag[_]]] {
		override def apply(i: DataInput): Named[Tag[_]] = read(i, i.readData(TagSpecIO))
		override def apply(o: DataOutput, t: Named[Tag[_]]) = write(o, t._1, t._2)

		private def read[A](i: DataInput, s: TagSpec[A]): Named[Tag[A]] = {
			(i.readData(s.name), Tag(i.readData(s.data))(s))
		}
		private def write[A](o: DataOutput, n: String, t: Tag[A]): Unit = {
			o.writeData(t.spec)
			o.writeData(n)(t.spec.name)
			o.writeData(t.value)(t.spec.data)
		}
	}
}
