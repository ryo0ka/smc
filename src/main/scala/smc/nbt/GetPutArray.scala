package smc.nbt

import java.io.{DataInput, DataOutput}

import smc.binary._

import scala.reflect.ClassTag

/** Defines (de)serialization of any [[Array]] whose data type has [[GetPut]] defined.
  */
class GetPutArray[A: ClassTag](dataIO: GetPut[A]) extends GetPutAbs[Array[A]] {
  override def get(in: DataInput): Array[A] = {
    Array.fill(in.readInt())(in.reads(dataIO))
  }

  override def put(out: DataOutput, arr: Array[A]) {
    out.writeInt(arr.length)
    arr.foreach(out.writes[A](_)(dataIO))
  }
}
