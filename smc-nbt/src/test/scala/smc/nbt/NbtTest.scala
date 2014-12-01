package smc.nbt

object NbtTest extends App {
	import java.io._
	import java.util.zip._

	val url = getClass.getResource("/level.dat").getFile
	val fin = new FileInputStream(url)
	val zin = new GZIPInputStream(fin)
	val din = new DataInputStream(zin)
	println(din.readNbt())
}
