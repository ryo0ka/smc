package smc.level

import smc.nbt._

trait Level {
	val version: Int
	val initialized: Boolean
	val levelName: String
	val seed: Long
	val generator: Generator

}
