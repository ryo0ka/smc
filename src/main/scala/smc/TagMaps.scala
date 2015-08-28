package smc

import java.io.{DataInput, DataOutput}
import DataIO._

import scala.collection.immutable._
import scala.reflect.runtime.universe._

trait TagMaps { this: Tags =>
	protected trait End { this: TagSpec[Null] =>
		final override val data = ioData[Null](_ => null, (_, _) => Unit)
		final override val name = ioData[String](_ => "", (_, _) => Unit)
		final override val ttag = typeTag[Null]
		val named: Named[Tag[Null]] = ("", Tag(null: Null)(this))
	}

	type TagMap = Map[String, Tag[_]]

	protected class TagMapIO(implicit end: End) extends IODataAbs[TagMap] {
		override def apply(i: DataInput) = {
			Iterator.continually(NamedTagIO(i)).takeWhile(!isEnd(_)).toMap
		}

		override def apply(o: DataOutput, m: TagMap) = {
			m.foreach {
				case n if isEnd(n) => throw new TagMapIllegalEndException(m)
				case n => o.writeData(n)
			}
			o.writeData(end.named)
		}

		private def isEnd(nt: Named[Tag[_]]): Boolean = {
			nt._2.spec.isInstanceOf[End]
		}
	}

	class TagMapIllegalEndException(val map: TagMap) extends RuntimeException
}
