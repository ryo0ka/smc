#smc-nbt

Minecraft NBT (Named Binary Tag) serialization for Scala projects.<br>
Just import `smc.nbt._` and everything is set!

##Functions

###Reading

	val in: DataInputStream = ???
	val (name: String, tag: Nbt[_]) = Nbt.enc(in)
	val (name, tag) = in.readNbt()

###Untagging

	val tag: Nbt[_] = ???
	val d: Double = tag.get
	val d = tag.get[Double]
	val ds: Seq[Double] = tag.getSeq //expecting TAG_List
	val e = tag.get[Regex] //Compile error: "Regex is not of NBT type."
	val b: Boolean = tag.get[Byte].toBool

###Tagging

	val version: Nbt[Int] = NbtInt(19133)
	val version: Nbt[Int] = Nbt(19133)
	val version: Nbt[Int] = 19133
	val e = Nbt("".r) //Compile error: "Regex is not of NBT type."
	val b: Nbt[Byte] = true.toByte

###Writing

	val out: DataOutputStream = ???
	val name: String = ???
	val tag: Nbt[_] = ???
	Nbt.dec(out, name -> tag)
	out.writeNbt(name -> tag)

##References

- [Minecraft Wiki: NBT format](http://minecraft.gamepedia.com/NBT_format) (11/15/2014)
