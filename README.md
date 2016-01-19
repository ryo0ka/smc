#smc

##smc.nbt

Named Binary Tags implementation in Scala, wastefully user-friendly and extensible.

Supports NBT v19132 and v19133, but it is made easy to modify/extend a system.

Benchmark is not yet done; the performance is unclear at the moment.

    // Demonstrates quick construction of a NBT system and its usage.
    import smc.enum._
    import smc.named._
    import smc.polyio._
    import smc.nbt._
  
    // Prepares whatever data type to (de)serialize.
    class Foo {...}
  
    // Implements a NBT system with whatever data types.
    object TagsImpl extends Tags with DirtyEnum with TagsUI {
  
      // Implements (de)serialization methods inside of TagsDef.
      class TagDefImpl[A] extends AbsTagDef[A] with DirtyElem {...}
      override type TagDef[A] = TagDefImpl[A]
  
      // Lists up whatever desired data types in desired order.
      val IntTag = new TagDefImpl[Int]
      val UTFTag = new TagDefImpl[String]
      val FooTag = new TagDefImpl[Foo]
      ...
    }
  
    // Imports (de)serialization functionality of defined tags.
    import TagsImpl._
  
    // Prepares fake I/O sources.
    import java.io.{DataInput, DataOutput}
    val in: DataInput = {...}
    val out:: DataOutput = {...}
  
    // Deserializes a Foo value named "fooo" from the head of the input source (or fails).
    val MyFoo = FooTag as "fooo"
    val MyFoo(foo) = in.readP
  
    // Serializes a Foo value named "fooo" to the output source.
    out.writeP(MyFoo(foo))