package smc.nbt.pickler

import java.io._

class BridgePickler[A](
	i: InputStream => A,
	o: (OutputStream, A) => Unit
) extends Pickler[A] {
	val pickle: Pickle[A] = { n =>
		new ByteArrayOutputStream {
			o(this, n)
			val b = buf
		}.b
	}
	val unpickle: Unpickle[A] = { in =>
		i(new InputStream {
			def read() = in.next()
		})
	}
}

object BridgePickler { self =>
	def toIterator(i: InputStream): BytesI = {
		Iterator.continually(i.read).takeWhile(_ != -1).map(_.toByte)
	}

	def write(o: OutputStream, s: BytesO): Unit = {
		s.foreach(b => o.write(b))
	}

	implicit class InputStreamOp(val i: InputStream) extends AnyVal {
		def toIterator: BytesI = self.toIterator(i)
	}

	implicit class OutputStreamOp(val o: OutputStream) extends AnyVal {
		def writeAll(s: BytesO): Unit = self.write(o, s)
	}

	//TODO Dramatic change to Stream-based
}
