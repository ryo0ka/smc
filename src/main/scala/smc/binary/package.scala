package smc

import java.io.{DataInput, DataOutput}

/**
  * Brings polymorphism into [[java.io]].
  *
  * {{{
  * val in: DataInput = ???
  *
  * in.readP[Int]: Int
  * in.readP[String]: String
  *
  * val out: DataOutput = ???
  *
  * out.writeP(3)
  * out.writeP("foo")
  * }}}
  *
  * Any types that an implicit [[smc.binary.GetPut]] instance is defined for
  * can be read/written with [[smc.binary.GetDataInput.reads()]] and [[smc.binary.PutDataOutput.writes()]]
  * that are added to [[java.io.DataInput]] and [[java.io.DataOutput]].
  *
  * Some [[smc.binary.GetPut]] instances are pre-defined in [[smc.binary]] for those types that
  * [[java.io.DataInput]] and [[java.io.DataOutput]] knows how to read/write already;
  * such as [[Byte]], [[Double]], [[String]](UTF-8), etc.
  *
  * Names of [[smc.binary.GetPut]] instances can be `FooIO` `FooPIO` or whatever like those.
  */
package object binary {

  /**
    * Shortcuts to types that define both [[Get]] and [[Put]].
    */
  type GetPut[A] = Get[A] with Put[A]

  /**
    * Generates an [[GetPut]] instance out of given read/write functions.
    */
  def getput[A](pi: DataInput => A, po: (DataOutput, A) => Unit): GetPut[A] = new GetPutImpl[A](pi, po)

  /**
    * Defines how to read the given type's values.
    * Make this instance implicit to enable [[GetDataInput.reads()]] on the given type.
    */
  trait Get[+A] {
    def get(in: DataInput): A
  }

  /**
    * Defines how to write the given type's values.
    * Make this instance implicit to enable [[PutDataOutput.writes()]] on the given type.
    */
  trait Put[-A] {
    def put(out: DataOutput, t: A): Unit
  }

  /**
    * Inherits both [[Get]] and [[Put]].
    * Instances of this type are also [[GetPut]].
    */
  trait GetPutAbs[A] extends Get[A] with Put[A]

  /**
    * Adds a polymorphic read function to a DataInput instance.
    */
  implicit class GetDataInput(val in: DataInput) extends AnyVal {
    final def reads[A: Get]: A = implicitly[Get[A]].get(in)
  }

  /**
    * Adds a polymorphic write function to a DataOutput instance.
    */
  implicit class PutDataOutput(val out: DataOutput) extends AnyVal {
    final def writes[A: Put](value: A): Unit = implicitly[Put[A]].put(out, value)
  }

  /**
    * Parameterises read/write functions of [[GetPutAbs]].
    */
  class GetPutImpl[A](pi: DataInput => A, po: (DataOutput, A) => Unit) extends Get[A] with Put[A] {
    override def get(in: DataInput): A = pi(in)

    override def put(out: DataOutput, a: A): Unit = po(out, a)
  }

  implicit object GetPutByte extends GetPutImpl[Byte](_.readByte(), _.writeByte(_))

  implicit object GetPutBool extends GetPutImpl[Boolean](_.readBoolean(), _.writeBoolean(_))

  implicit object GetPutChar extends GetPutImpl[Char](_.readChar(), _.writeChar(_))

  implicit object GetPutShort extends GetPutImpl[Short](_.readShort(), _.writeShort(_))

  implicit object GetPutInt extends GetPutImpl[Int](_.readInt(), _.writeInt(_))

  implicit object GetPutLong extends GetPutImpl[Long](_.readLong(), _.writeLong(_))

  implicit object GetPutFloat extends GetPutImpl[Float](_.readFloat(), _.writeFloat(_))

  implicit object GetPutDouble extends GetPutImpl[Double](_.readDouble(), _.writeDouble(_))

  implicit object GetPutString extends GetPutImpl[String](_.readUTF(), _.writeUTF(_))

}
