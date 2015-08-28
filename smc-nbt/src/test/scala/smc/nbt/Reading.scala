package smc.nbt

object Reading extends App {
	import java.io._
	import java.util.zip._

import smc.nbt.Tags19133._

	val url = getClass.getResource("/level.dat").getFile
	val fin = new FileInputStream(url)
	val zin = new GZIPInputStream(fin)
	val din = new DataInputStream(zin)
	object Root extends TagItem[TagMap]("")
	object Data extends TagItem[TagMap]("Data")
	val Root(Data(data)) = din.readTag()
	println(data)
}
