package smc.nbt.api

import scala.language.implicitConversions
import java.io.{DataInput, DataOutput}
import dataio._

trait Fronts { this: Tags =>
	implicit def tag[A: TagSpec](v: A): Tag[A] = Tag(v)

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
		def apply(t: T): Named[Tag[T]] = {
			(name, Tag(t))
		}
		def unapply(n: Named[Tag[_]]): Option[T] = n match {
			case (this.name, t) => t.getOpt[T]
			case _ => None
		}
		def unapply(m: Map[String, Tag[_]]): Option[T] = {
			m.lift(name).flatMap(_.getOpt[T])
		}
	}
}
