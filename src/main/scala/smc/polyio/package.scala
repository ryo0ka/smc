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
 * Any types that an implicit [[smc.polyio.PolyIO]] instance is defined for
 * can be read/written with [[smc.polyio.PolyDataInput.readP()]] and [[smc.polyio.PolyDataOutput.writeP()]]
 * that are added to [[java.io.DataInput]] and [[java.io.DataOutput]].
 *
 * Some [[smc.polyio.PolyIO]] instances are pre-defined in [[smc.polyio]] for those types that
 * [[java.io.DataInput]] and [[java.io.DataOutput]] knows how to read/write already;
 * such as [[Byte]], [[Double]], [[String]](UTF-8), etc.
 *
 * Names of [[smc.polyio.PolyIO]] instances can be `FooIO` `FooPIO` or whatever like those.
 */
package object polyio {

  /**
   * Defines how to read the given type's values.
   * Make this instance implicit to enable [[PolyDataInput.readP()]] on the given type.
   */
  trait PolyI[+A] {
    def read(in: DataInput): A
  }

  /**
   * Defines how to write the given type's values.
   * Make this instance implicit to enable [[PolyDataOutput.writeP()]] on the given type.
   */
  trait PolyO[-A] {
    def write(out: DataOutput, t: A): Unit
  }

  /**
   * Adds a polymorphic read function to a DataInput instance.
   */
  implicit class PolyDataInput(val in: DataInput) extends AnyVal {
    final def readP[A: PolyI]: A = implicitly[PolyI[A]].read(in)
  }

  /**
   * Adds a polymorphic write function to a DataOutput instance.
   */
  implicit class PolyDataOutput(val out: DataOutput) extends AnyVal {
    final def writeP[A: PolyO](value: A): Unit = implicitly[PolyO[A]].write(out, value)
  }

  /**
   * Shortcuts to types that define both [[PolyI]] and [[PolyO]].
   */
  type PolyIO[A] = PolyI[A] with PolyO[A]

  /**
   * Inherits both [[PolyI]] and [[PolyO]].
   * Instances of this type are also [[PolyIO]].
   */
  trait AbsPolyIO[A] extends PolyI[A] with PolyO[A]

  /**
   * Parameterises read/write functions of [[AbsPolyIO]].
   */
  class ImplPolyIO[A](pi: DataInput => A, po: (DataOutput, A) => Unit) extends PolyI[A] with PolyO[A] {
    override def read(in: DataInput): A = pi(in)
    override def write(out: DataOutput, a: A): Unit = po(out, a)
  }

  /**
   * Generates an [[PolyIO]] instance out of given read/write functions.
   */
  def polyIO[A](pi: DataInput => A, po: (DataOutput, A) => Unit): PolyIO[A] = new ImplPolyIO[A](pi, po)

  // Pre-defined PolyIO's
  implicit object ByteIO   extends ImplPolyIO[Byte   ](_.readByte()   , _.writeByte(_)   )
  implicit object BoolIO   extends ImplPolyIO[Boolean](_.readBoolean(), _.writeBoolean(_))
  implicit object CharIO   extends ImplPolyIO[Char   ](_.readChar()   , _.writeChar(_)   )
  implicit object ShortIO  extends ImplPolyIO[Short  ](_.readShort()  , _.writeShort(_)  )
  implicit object IntIO    extends ImplPolyIO[Int    ](_.readInt()    , _.writeInt(_)    )
  implicit object LongIO   extends ImplPolyIO[Long   ](_.readLong()   , _.writeLong(_)   )
  implicit object FloatIO  extends ImplPolyIO[Float  ](_.readFloat()  , _.writeFloat(_)  )
  implicit object DoubleIO extends ImplPolyIO[Double ](_.readDouble() , _.writeDouble(_) )
  implicit object StringIO extends ImplPolyIO[String ](_.readUTF()    , _.writeUTF(_)    )
}
