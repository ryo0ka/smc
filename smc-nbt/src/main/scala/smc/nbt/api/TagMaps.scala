package smc.nbt.api

import java.io.{DataInput, DataOutput}

import dataio._

import scala.collection.immutable._

trait TagMaps { this: Tags with Ends =>
	type TagMap = Map[String, Tag[_]]

	protected class TagMapIO(implicit end: End) extends IODataAbs[TagMap] {
		override def apply(i: DataInput): TagMap = {
			Iterator.continually(NamedTagIO(i)).takeWhile(notEnd).toMap
		}

		override def apply(o: DataOutput, m: TagMap): Unit = {
			m.foreach(o.writeData(_))
			o.writeData(end.named)
		}

		private def notEnd(nt: Named[Tag[_]]): Boolean = {
			!nt._2.spec.isInstanceOf[End]
		}
	}
}
