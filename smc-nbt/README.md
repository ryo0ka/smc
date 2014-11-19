#smc-nbt

Minecraft NBT (Named Binary Tag) functions in Scala.

ehg-nbt supports NBT serialization within type-safe operations.

##Examples

###Deserializing a tag

	val in: InputStream = ???
	val (name: String, tag: NbtN) = Nbt.unpickle(i)
	val (name, NbtByte(byte)) = Nbt.unpickle(i)

###Serializing a tag

	val out: OutputStream = ???
	val name: String = ???
	val tag: NbtN = ???
	Nbt.pickle(out, (name, tag))

###Extracting a tag's value

	val tag: NbtN = ???
	val d: Double = tag[Double]
	val NbtDouble(d) = tag
	val d: Option[Double] = tag.as[Double]
	val foo = tag[Regex] //Compile error; Regex is not of NBT type

###Constructing tags

Note that `NbtMap` is TAG_Compound and `immutable.Map[String, NbtN]`.

	val version: Nbt[Int] = 19133
	val level: Nbt[NbtMap] = NbtMap(
	  "version" -> version,
	  "LevelName" -> "New World"
	  "initialized" -> true.toByte
	  "foo" -> "".r //Compile error; Regex is not of NBT type
	)

##Directions

In order to avail functions, please follow these steps:

1. implement a concrete class/object of `BasePicklers` trait
2. construct `NbtEnv` class with it
3. import everything in it

Example code:

	import smc.nbt.NbtEnv
	import smc.nbt.pickler.BasePicklers

	val base: BasePicklers = ???
	val e = new NbtEnv(base)
	import e._
	//Here all items are available

Trait `BasePicklers` is defined as:

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

Trait `Pickler` is defined as:

	package smc.nbt.pickler

	trait Pickler[A] {
	  val pickle: (OutputStream, A) => Unit
	  val unpickle: InputStream => A
	}

	object Pickler {
	  def apply[A](i: Unpickle[A], o: Pickle[A]) = new Pickler[A] {
	    override val pickle = o
	    override val unpickle = i
	  }
	}

All tags' names are optimized in the Java common form:

|Original name|In smc-nbt|
|:--|:--|
|TAG_End|NbtEnd|
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

##References

- [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)
