#smc

Minecraft world I/O utility for Scala projects.

##smc-nbt

NBT (Named Binary Tag) serialization.

	val dat: DataInputStream = ???
	val ("", NbtMap(root)) = dat.readNbt()
	val NbtInt(version) = root("version") //Int

	val dat: DataOutputStream = ???
	dat.writeNbt("" -> root)

##smc-level

Level/Region/Chunk/Section/Block operation, yet work in progress.