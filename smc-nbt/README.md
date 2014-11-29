#smc-nbt

Minecraft NBT (Named Binary Tag) serialization for Scala projects.

Note that this library makes minimum effort for the I/O performance.<br>
If you are looking for fast functions, this library may not be for you.

##Examples

###Reading

	val in: DataInputStream = ???
	val (name: String, tag: Nbt[_]) = in.readNbt()

###Untagging

	val tag: Nbt[_] = ???
	val d: Double = tag.get
	val d = tag.get[Double]
	val ds: Seq[Double] = tag.getSeq
	val foo = tag.get[Regex] //Compile error; Regex is not of NBT type

###Tagging

	val version: Nbt[Int] = NbtInt(19133)
	val version: Nbt[Int] = Nbt(19133)
	val version: Nbt[Int] = 19133
	val foo = Nbt("".r) //Compile error; Regex is not of NBT type

###Writing

	val out: DataOutputStream = ???
	val name: String = ???
	val tag: Nbt[_] = ???
	out.writeNbt(name -> tag)

##Directions

###How to Start

	import smc.nbt._

##References

- [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)
