package smc.enum

/**
  * A not very safe instance of [[Enum]].
  */
trait DirtyEnum extends Enum {
  protected override type EnumElement <: DirtyElement
  private val values = collection.mutable.Buffer[EnumElement]()

  protected final override def element(ordinal: Int): EnumElement = values(ordinal)

  protected final override def ordinal(element: EnumElement): Int = element.ordinal

  protected trait DirtyElement {
    val ordinal = values.size
    values += this.asInstanceOf[EnumElement] //Wow XD
  }

}