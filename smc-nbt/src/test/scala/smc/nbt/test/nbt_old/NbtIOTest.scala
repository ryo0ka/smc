package smc.nbt.test.nbt_old

object NbtIOTest extends App {
	import java.io._
	import java.util.zip._

	val url = getClass.getResource("/level.dat").getFile
	val fin = new FileInputStream(url)
	val zin = new GZIPInputStream(fin)
	val din = new DataInputStream(zin)
	println(din.readNbt())
}
