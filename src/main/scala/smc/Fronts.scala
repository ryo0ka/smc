package smc

import scala.language.implicitConversions
import java.io.{DataInput, DataOutput}
import DataIO._

trait Fronts { this: Tags =>
	implicit def tag[A: TagSpec](v: A): Tag[A] = Tag(v)

	implicit class TagOp[T](t: Tag[T]) {
		def get[A: TagSpec]: A = {
			t.value.asInstanceOf[A]
		}
		def getOpt[B: TagSpec]: Option[B] = {
			val tA = t.spec.ttag.tpe
			val tB = implicitly[TagSpec[B]].ttag.tpe
			if (tA <:< tB) Some(get[B]) else None
		}
	}

	protected trait Front[A] { this: TagSpec[A] =>
		def apply(v: A): Tag[A] = Tag(v)(this)
		def unapply(t: Tag[_]): Option[A] = t.getOpt[A](this)
	}

	implicit class TagInput(i: DataInput) {
		def readTag() = i.readData[Named[Tag[_]]]
	}

	implicit class TagOutput(o: DataOutput) {
		def writeTag(t: Named[Tag[_]]) = o.writeData(t)
	}

	class TagItem[T: TagSpec](val name: String) {
		def extract(n: Tag[_]): Option[T] = {
			n.getOpt[T]
		}
		def apply(t: T): Named[Tag[T]] = {
			(name, Tag(t))
		}
		def unapply(n: Named[Tag[_]]): Option[T] = n match {
			case (this.name, t) => extract(t)
			case _ => None
		}
		def unapply(s: Traversable[Named[Tag[_]]]): Option[T] = s match {
			case m: Map[String, Tag[_]] => m.get(name).flatMap(extract)
			case n => n.find(_._1 == name).flatMap(nt => extract(nt._2))
		}
	}
}
