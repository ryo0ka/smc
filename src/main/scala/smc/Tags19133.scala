package smc

import DataIO._

trait Tags19133 extends Tags19132 {
	implicit val IntsSeq = new SI(new SeqIO(ioInt))
}

object Tags19133 extends Tags19133
