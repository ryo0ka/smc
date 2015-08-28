package smc.nbt.api

import dataio._

import scala.reflect.runtime.universe._

trait SpecUtils { this: Tags =>
	protected class Impl[T: TypeTag](
		override val data: IOData[T],
		override val name: IOData[String] = ioString) { this: TagSpecAbs[T] =>
		override val ttag = typeTag[T]
	}
}
