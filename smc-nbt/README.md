#smc-nbt

Minecraft NBT (Named Binary Tag) serialization for Scala projects.

##Examples

###Reading

	val in: InputStream = ???
	val (name: String, tag: Nbt[_]) = Nbt.unpickle(i)
	val (name, NbtByte(b: Byte)) = Nbt.unpickle(i)

###Untagging

	val tag: Nbt[_] = ???
	val NbtDouble(d) = tag
	val d: Double = tag.get
	val d = tag.get[Double]
	val ds: Seq[Double] = tag.get[NbtSeq[Double]]
	val foo = tag.get[Regex] //Compile error; Regex is not of NBT type

###Tagging

	val version: Nbt[Int] = NbtInt(19133)
	val version: Nbt[Int] = Nbt(19133)
	val version: Nbt[Int] = 19133 //Function `Nbt` is implicit
	val foo = Nbt("".r) //Compile error; Regex is not of NBT type

###Writing

	val out: OutputStream = ???
	val name: String = ???
	val tag: Nbt[_] = ???
	Nbt.pickle(out, (name, tag))

##Directions

###How to Start

It is not so instant that you've expected:

1. implement `BasePicklers` trait
2. construct `NbtEnv` class with it
3. import all the contents

Example code:

	import smc.nbt.NbtEnv
	import smc.nbt.pickler.BasePicklers

	val base: BasePicklers = ???
	val e = new NbtEnv(base)
	import e._
	//Here all items are available

###pickler\BasePicklers.scala

	package smc.nbt.pickler

	trait BasePicklers {
      val byte: Pickler[Byte]
      val short: Pickler[Short]
      val int: Pickler[Int]
      val long: Pickler[Long]
      val float: Pickler[Float]
      val double: Pickler[Double]
      val string: Pickler[String]
    }

###pickler\Pickler.scala

	package smc.nbt.pickler

	trait Pickler[A] {
	  override def pickle(o: O, n: A): Unit
	  override def unpickle(i: I): A
	}

	object Pickler {
	  type I = InputStream
	  type O = OutputStream
	  type Pickle[A] = (O, A) => Unit
	  type Unpickle[A] = I => A

	  def apply[A](enc: Unpickle[A], dec: Pickle[A]) = new Pickler[A] {
	    override def pickle(o: O, n: A): Unit = dec
	    override def unpickle(i: I): A = enc
	  }
	}

###NbtEnv.scala (signatures)

	package smc.nbt

	import scala.collection.immutable._

	final class NbtEnv(base: BasePicklers) {
	  sealed trait NbtSpec[T] {
	    def unapply(n: Nbt[_]): Option[T]
	    def apply(v: T): Nbt[T]
	  }

	  implicit sealed case class Nbt[T: NbtSpec](value: T) {
	    def get[U: NbtSpec]: U
	  }

	  sealed case class NbtSeq[T: NbtSpec](value: Seq[T]) {
	    def get[U: NbtSpec]: Seq[U]
	  }

	  type NbtMap = Map[String, Nbt[_]]

	  object NbtEnd extends Nbt[Null] with NbtSpec[Null] {
	    val named: (String, Nbt[Null])
	  }

	  implicit object NbtByte   extends NbtSpec[Byte     ]
	  implicit object NbtShort  extends NbtSpec[Short    ]
	  implicit object NbtInt    extends NbtSpec[Int      ]
	  implicit object NbtLong   extends NbtSpec[Long     ]
	  implicit object NbtFloat  extends NbtSpec[Float    ]
	  implicit object NbtDouble extends NbtSpec[Double   ]
	  implicit object NbtBytes  extends NbtSpec[Seq[Byte]]
	  implicit object NbtString extends NbtSpec[String   ]
	  implicit object NbtSeq    extends NbtSpec[NbtSeq[_]]
	  implicit object NbtMap    extends NbtSpec[NbtMap   ]
	  implicit object NbtInts   extends NbtSpec[Seq[Int] ]

	  object Nbt extends Pickler[(String, Nbt[_])]
	}

##References

- [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)
