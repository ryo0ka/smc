ehg-nbt (W.I.P)
===

Minecraft NBT (Named Binary Tag) functions in Scala.

Reference: [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)

	//Setup environment
	import smc.nbt._
	val base: pickle.BasePicklers = ???
	val e = new NbtEnv(base)
	import e._

	//Extract tags from byte stream
	val i: Iterator[Byte] = ???
	val n: NbtN = Nbt.unpickle(i)

	//Convert tags to byte stream
	val n: NbtN = ???
	val o: Seq[Byte] = Nbt.pickle(n)

	//Extract values from tags
	val n: NbtN = ???
	val root: NbtMap = n[NbtMap]

	//Extract values safely
	val n: NbtN = ???
	val root: Option[NbtMap] = n.as[NbtMap]

	//Construct tags
	val version: Nbt[Int] = 19133
	val level: Nbt[NbtMap] = NbtMap(
	  "version" -> version,
	  "LevelName" -> "New World"
	  "foo" -> "".r //compile error; Regex is not of NBT type.
	)
	val out: Seq[Byte] = Nbt.pickle(level)

|original name|in smc-nbt|
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
