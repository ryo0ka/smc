package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.polyio._

import scala.reflect.ClassTag

/** Defines (de)serialization of any [[Array]] whose data type has [[PolyIO]] defined.
  */
class ArrayIO[A: ClassTag](dataIO: PolyIO[A]) extends AbsPolyIO[Array[A]] {
  override def read(in: DataInput): Array[A] = {
    Array.fill(in.readInt())(in.readP(dataIO))
  }
  override def write(out: DataOutput, arr: Array[A]) {
    out.writeInt(arr.length)
    arr.foreach(out.writeP[A](_)(dataIO))
  }
}
