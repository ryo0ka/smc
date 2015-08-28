package smc.nbt

import dataio._
import api._

trait Tags19133 extends Tags19132 {
	implicit val IntsSeq = new SI(new SeqIO(ioInt))
}

object Tags19133 extends Tags19133
