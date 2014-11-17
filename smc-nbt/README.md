ehg-nbt
===

Minecraft NBT (Named Binary Tag) functions in Scala.

ehg-nbt supports NBT serialization and type-safe operations.

Code Example
---

	//Setting up the environment
	import smc.nbt._
	val base: pickler.BasePicklers = ???
	val e = new NbtEnv(base)
	import e._

	//Extracting a tag from a byte stream
	val i: Iterator[Byte] = ???
	val n: NbtN = Nbt.unpickle(i)

	//Converting a tag into a byte stream
	val n: NbtN = ???
	val o: Seq[Byte] = Nbt.pickle(n)

	//Extracting a tag's value
	val n: NbtN = ???
	val root: NbtMap = n[NbtMap]
	val foo = n[Regex] //Compile error; Regex is not of NBT type

	//Extracting the value safely
	val n: NbtN = ???
	val root: Option[NbtMap] = n.as[NbtMap]

	//Constructing tags
	val version: Nbt[Int] = 19133
	val level: Nbt[NbtMap] = NbtMap(
	  "version" -> version,
	  "LevelName" -> "New World"
	  "initialized" -> true.toByte
	  "foo" -> "".r //Compile error; Regex is not of NBT type
	)
	val out: Seq[Byte] = Nbt.pickle(level)

API Directions
---

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

External References
---

- [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)
