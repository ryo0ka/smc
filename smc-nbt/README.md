#smc-nbt

Minecraft NBT (Named Binary Tag) serialization for Scala projects.

##Examples

###Reading

	val in: DataInputStream = ???
	val (name: String, tag: Nbt[_]) = Nbt.enc(i)

###Untagging

	val tag: Nbt[_] = ???
	val d: Double = tag.get
	val d = tag.get[Double]
	val ds: Seq[Double] = tag.getSeq
	val foo = tag.get[Regex] //Compile error; Regex is not of NBT type

###Tagging

	val version: Nbt[Int] = NbtInt(19133)
	val version: Nbt[Int] = Nbt(19133)
	val version: Nbt[Int] = 19133 //Function `Nbt` is implicit
	val foo = Nbt("".r) //Compile error; Regex is not of NBT type

###Writing

	val out: DataOutputStream = ???
	val name: String = ???
	val tag: Nbt[_] = ???
	Nbt.dec(out, (name, tag))

##Directions

###How to Start

	import smc.nbt._

##References

- [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)
