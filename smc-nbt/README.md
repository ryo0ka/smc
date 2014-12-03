#smc-nbt

Minecraft NBT (Named Binary Tag) serialization and type-safe operation for Scala projects.

##Motivation

How to modify the level name in a `level.dat` file using smc-nbt:

	val file: File = ???
	val in = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))
	val out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))

	import smc.nbt._

	val ("", NbtMap(root)) = in.readNbt
	var NbtMap(data) = root("Data")
	data += "LevelName" -> "Shirakawa"
	out.writeNbt("" -> Map("Data" -> data))

##Functions
Import `smc.nbt._` and every function is available.

Note that tag names are optimized in the Java manner:

|Original name |nbt-smc name (type)  |
|:-------------|:--------------------|
|TAG_End       |NbtEnd(`Null`)       |
|TAG_Byte      |NbtByte              |
|TAG_Short     |NbtShort             |
|TAG_Int       |NbtInt               |
|TAG_Long      |NbtLong              |
|TAG_Float     |NbtFloat             |
|TAG_Double    |NbtDouble            |
|TAG_Byte_Array|NbtBytes(`Seq[Byte]`)|
|TAG_String    |NbtString            |
|TAG_List      |NbtSeq(`NbtSeq`)     |
|TAG_Compound  |NbtMap(`NbtMap`)     |
|TAG_Int_Array |NbtInts(`Seq[Int]`)  |

###Reading
A tag is stored with a name in a byte stream.<br>
Use `NbtIO#enc(DataInputStream):(String, Nbt[_])` to read the pair.<br>
Or use a friendly shortcut `DataInputStream#readNbt():(String, Nbt[_])`.

	val in: DataInputStream = ???
	val (name, tag) = NbtIO.enc(in)
	val (name, tag) = in.readNbt()

###Untagging
A read `Nbt` instance's type parameter is erased and therefore `Any`.

Use `Nbt[_]#get[A]:A` to cast and extract the value.<br>
Or use `#getOpt[A]:Option[A]` for safety.

	val tag: Nbt[_] = ???
	val b = tag.get[Byte]
	val ob = tag.getOpt[Byte]

Use `#seq[A]:Seq[A]` and `#seqOpt[A]:Option[Seq[A]]` to untag TAG_List.<br>
You may read [about NbtSeq](#seq) for the detail.

	val sb = tag.seq[Byte]
	val osb = tag.seqOpt[Byte]

Impossible types will cause a compile error.

	tag.get[Regex] //croaks "Regex is not of NBT type."

You may also use `NbtSpec[A]#unapply(Nbt[_]):Option[A]`.<br>
Read [about NbtSpec](#spec) for the detail.

	tag match {
	  case NbtByte(n) =>
	  case NbtString(n) =>
	  case NbtSeq(n) =>
	  case NbtMap(n) =>
	}

	val NbtMap(root) = tag

###Tagging

Use `Nbt[A](A):Nbt[A]` to tag a value.<br>
Also confirmed types will be implicily converted to `Nbt`.

	val tag: Nbt[String] = Nbt("New World")
	val tag: Nbt[String] = "New World"

Use `NbtSeq#apply[A](A):NbtSeq[A]` to make a TAG_List.<br>
Read [about NbtSeq](#seq) for the detail.

	val ss = Seq("a", "b", "c")
	val tag = NbtSeq(ss)

Impossible types will cause a compie error.

	Nbt("".r) //croaks "Regex is not of NBT type."

You may also use `NbtSpec[A]#apply(A):Nbt[A]`.<br>
Read [about NbtSpec](#spec) for the detail.

	val tag = NbtString("New World")

###Writing

Use `NbtIO#dec(DataOutputStream,(String, Nbt[_])):Unit`,<br>
or a shortcut `DataOutputStream#writeNbt((String, Nbt[_])):Unit`.

	val out: DataOutputStream = ???
	NbtIO.dec(out, name -> tag)
	out.writeNbt(name -> tag)

###Boolean
`Boolean` is often used as another representation of `Byte` in NBT operations.<br>
Use `Byte#toBool:Boolean` and `Boolean#toByte:Byte` for the conversion.<br>

	val b = nbt.get[Byte].toBool
	val b: Nbt[Byte] = true.toByte

Note that `Nbt[Boolean]` never exists (read [about NbtSpec](#spec) for the detail.)

##Details
###Nbt<a name="nbt"></a>
`Nbt` is a trait that represents any tags.

	trait Nbt[A] {
	  val value: A
	  val spec: NbtSpec[A]
	}

Two methods are injected to any `Nbt` instances to extract their type-erased value.<br>
Read [about NbtSpec](#spec) for the detail.

	def get[A: NbtSpec]: A
	def getOpt[A: NbtSpec]: Option[A]

Another pair of methods are also injected specifically to untag TAG_List.<br>
Read [about NbtSpec](#spec) for the detail.

	def seq[A: NbtSpec]: Seq[A]
	def seqOpt[A: NbtSpec]: Option[Seq[A]]

###NbtSeq<a name="seq"></a>
`NbtSeq` is **not** `Nbt` but another trait that represents the contents of Tag_List.

	trait NbtSeq[A] {
	  val value: Seq[A]
	  val spec: NbtSpec[A]
	}

A `TAG_List` of `String` in smc-nbt is printed out as:

	Nbt(NbtSeq(Seq("a", "b", "c")))

Two methods are injected to any `NbtSeq` instances to extract their type-erased `Seq`.<br>
Read [about NbtSpec](#spec) for the detail.

	def get[A: NbtSpec]: Seq[A]
	def getOpt[A: NbtSpec]: Option[Seq[A]]

Untagging `Nbt[NbtSeq[_]]` incorporates two type operations.

	val sb = tag.get[NbtSeq[_]].get[Byte]
	val osb = tag.getOpt[NbtSeq[_]].flatMap(_.getOpt[Byte])

Use `Nbt`'s shortcut methods `#seq[A]:Seq[A]` and `#seqOpt[A]:Option[Seq[A]]` instead.

	val sb = tag.seq[Byte]
	val osb = tag.seqOpt[Byte]

Making a TAG_List requires an explicit conversion from `Seq[A]` to `NbtSeq[A]`.

	val sb: Seq[Byte] = ???
	val tag = Nbt(sb) //becomes TAG_Byte_Array
	val nsb = NbtSeq(sb)
	val tag = Nbt(nsb) //becomes TAG_List of Byte

###NbtMap
`NbtMap` is **not** `Nbt` but any types that inherit `scala.collection.immutbale.Map[String, Nbt[_]]`.<br>
`NbtMap` represents the contents of TAG_Compound.

A TAG_Compound in smc-nbt is printed out as:

	Nbt(Map("LevelName" -> Nbt("New World"), "version" -> Nbt(19133)))

###NbtSpec<a name="spec"></a>
An instance of `NbtSpec[A]` proves that the type `A` is of NBT.<br>
The construction is sealed but the instances are visible from anywhere.

Use `NbtSpec$#apply[A]: NbtSpec[A]` to take a shortcut to `implicitly[NbtSpec[A]]`.

	def foo[A: NbtSpec] = {
	  val spec = NbtSpec[A]
	}

Use `NbtSpec[A]#apply(A):Nbt[A]` for clearer construction of tags.

	val tag = NbtInt(3)

Use `NbtSpec[A]#unapply(Nbt[_]):Option[Nbt[A]]` for pattern matching and clearer untagging.

	tag match {
	  case NbtByte(n) =>
	  case NbtString(n) =>
	  case NbtSeq(n) =>
	  case NbtMap(n) =>
	}

	val NbtMap(root) = tag

##References

- [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)
