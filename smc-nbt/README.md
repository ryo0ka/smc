#smc-nbt

Minecraft NBT (Named Binary Tag) functions in Scala.

ehg-nbt supports NBT serialization within type-safe operations.<br>
The interface is designed to fit to any external serialization libraries.

##Code Examples

###Deserializing a tag

	val i: Iterator[Byte] = ???
	val n: NbtN = Nbt.unpickle(i)

###Serializing a tag

	val n: NbtN = ???
	val o: Seq[Byte] = Nbt.pickle(n)

###Extracting a tag's value

Note that `NbtMap` is TAG_Compound.

	val n: NbtN = ???
	val root: NbtMap = n[NbtMap]
	val root: Option[NbtMap] = n.as[NbtMap]
	val foo = n[Regex] //Compile error; Regex is not of NBT type

###Constructing tags

	val version: Nbt[Int] = 19133
	val level: Nbt[NbtMap] = NbtMap(
	  "version" -> version,
	  "LevelName" -> "New World"
	  "initialized" -> true.toByte
	  "foo" -> "".r //Compile error; Regex is not of NBT type
	)

##API Directions

###How to Start

In order to launch the NBT functions, please follow these steps:

1. somehow construct the `BasePicklers` trait
2. pass it to the `NbtEnv` constructor
3. import everything in the `NbtEnv` instance


	import smc.nbt.NbtEnv
	import smc.pickler.BasePicklers

	val base: BasePicklers = ???
	val e = new NbtEnv(base)
	import e._
	//Here all items are available

Trait `BasePicklers` is defined as:

	package smc.pickler

	trait BasePicklers {
      val byte: Pickler[Byte]
      val short: Pickler[Short]
      val int: Pickler[Int]
      val long: Pickler[Long]
      val float: Pickler[Float]
      val double: Pickler[Double]
      val string: Pickler[String]
    }

Trait `Pickler` is defined as:

	package smc.pickler

	trait Pickler[A] {
	  val pickle: (OutputStream, A) => Unit
	  val unpickle: InputStream => A

	  val bpickle: A => Seq[Byte]
	  val bunpickle: Iterator[Byte] => A
	}

If your serializatoin library is based on streams:

	def serializeString(out: OutputStream, str: String): Unit = ???
	def deserializeString(in: InputStream): String = ???

	import smc.picler._

	object MyBasePicklers extends BasePicklers {
	  override val string = Pickler(deserializeString, serializeString)
	  ...
	}
	...

If your serialization library is based on byte sequences:

	def pickleString(str: String): Seq[Byte] = ???
	def unpickleString(in: Iterator[Byte]): String = ???

	import smc.pickler._

	object MyBasePicklers extends BasePicklers {
		override val string = Pickler(unpickleString, pickleString)
		...
	}
	...

###Tag names

All tags' names are optimized in the Java common form:

|Original name|In smc-nbt|
|:--|:--|
|TAG_End|NbtEnd (invisible)|
|TAG_Byte|NbtByte|
|TAG_Short|NbtShort|
|TAG_Int|NbtInt|
|TAG_Long|NbtLong|
|TAG_FLoat|NbtFloat|
|TAG_Double|NbtDouble|
|TAG_Byte_Array|NbtBytes|
|TAG_String|NbtString|
|TAG_List|NbtSeq|
|TAG_Compound|NbtMap|
|TAG_Int_Array|NbtInts|

##External References

- [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)
