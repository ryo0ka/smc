#smc

Minecraft world I/O utility for Scala projects.

##smc-nbt

NBT (Named Binary Tag) serialization.

	val level: DataInputStream = ...
	val ("", NbtMap(root)) = Nbt.enc(level)
	val NbtInt(version) = root("version")

##smc-level

Level/Region/Chunk/Section/Block operation, yet work in progress.