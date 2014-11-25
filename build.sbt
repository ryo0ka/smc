name := "smc"

val `smc-nbt` = project.in(file("smc-nbt"))
val `smc-level` = project.in(file("smc-level")).dependsOn(`smc-nbt`)