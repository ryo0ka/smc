#smc-nbt

Minecraft NBT (Named Binary Tag) serialization and type-safe operations for Scala projects.

##Examples

###Reading

	val in: InputStream = ???
	val (name: String, tag: Nbt[_]) = Nbt.unpickle(i)
	val (name, NbtByte(b: Byte)) = Nbt.unpickle(i)

###Writing

	val out: OutputStream = ???
	val name: String = ???
	val tag: Nbt[_] = ???
	Nbt.pickle(out, (name, tag))

###Untagging

	val tag: Nbt[_] = ???
	val NbtDouble(d) = tag
	val d: Double = tag.get
	val d = tag.get[Double]
	val foo = tag.get[Regex] //Compile error; Regex is not of NBT type

###Tagging

	val version: Nbt[Int] = Nbt(19133)
	val version: Nbt[Int] = 19133 //Function `Nbt` is implicit
	val foo = Nbt("".r) //Compile error; Regex is not of NBT type

##Directions

###How to Start

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
	  val pickle: Pickle[A]
	  val unpickle: Unpickle[A]
	}

	object Pickler {
	  def apply[A](i: Unpickle[A], o: Pickle[A]) = new Pickler[A] {
	    override val pickle = o
	    override val unpickle = i
	  }
	}

###pickler\package.scala

	package smc.nbt

	package object pickler {
	  type I = InputStream
	  type O = OutputStream
	  type Pickle[A] = (O, A) => Unit
	  type Unpickle[A] = I => A
	}

###NbtEnv.scala (pseudo)

	package smc.nbt

	import scala.collection.immutable._

	final class NbtEnv(base: BasePicklers) {
	  sealed trait NbtSpec[T] {
	    def unapply(n: Nbt[_]): Option[T]
	  }

	  implicit final case class Nbt[T: NbtSpec](value: T) {
	    def get[U: NbtSpec]: U
	  }

	  object Nbt extends Pickler[Nbt[_]]

	  final case class NbtSeq[T: NbtSpec](value: Seq[T]) {
	    def get[U: NbtSpec]: Seq[U]
	  }

	  type NbtMap = Map[String, Nbt[_]]

	  implicit object NbtEnd    extends NbtSpec[Null     ]
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
	}

##References

- [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)
