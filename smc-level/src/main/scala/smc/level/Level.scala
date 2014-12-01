package smc.level

case class Level(
	version: Int,
	initialized: Boolean,
	levelName: String,
	seed: Long,
	generator: Generator)