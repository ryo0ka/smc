#smc

Minecraft NBT (Named Binary Tag) implementation in Scala. This library is developed researching the capacity of the Scala type system, compared to those of Java's and Haskell's. This library is published to show off my understanding to Scala. No attention is paid to the practical I/O performance. If you're here looking for a fine NBT implementation in Scala, I highly recommend you find some C/C++ library, wrap it with some FFI in Java and call it from Scala.

	val dat: DataInputStream = ???
	val ("", NbtMap(root)) = dat.readNbt()
	val NbtInt(version) = root("version") //Int

	val dat: DataOutputStream = ???
	dat.writeNbt("" -> root)