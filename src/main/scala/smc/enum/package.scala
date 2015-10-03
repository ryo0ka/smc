package smc

package object enum {
  /**
   * Dual-maps elements in a specific set to distinct numbers.
   *
   * Both conversions between those elements and their numbers must be constant time.
   */
  trait Enum {
    /**
     * Defines the type of elements to map to/from numbers.
     */
    protected type Elem

    /**
     * Maps a given integer to a corresponding element.
     * @param ordinal a number to search the corresponding element for.
     * @return an element that is mapped to the given number.
     * @throws NoSuchElementException or whatever exceptions if no elements are mapped to the given number.
     */
    protected def element(ordinal: Int): Elem

    /**
     * Maps a given element to a corresponding number.
     * @param element an element to search the corresponding number for.
     * @return a number that is mapped to the given element.
     * @throws NoSuchElementException or whatever exceptions if no numbers are mapped to the given element.
     */
    protected def ordinal(element: Elem): Int
  }

  /**
   * A very much safe instance of [[Enum]].
   *
   * The element type (substituted to [[Enum.Elem]])
   * must inherit the [[DirtyEnum.DirtyElem]] trait,
   * otherwise the system wouldn't work.
   *
   * Note that [[DirtyEnum.DirtyElem]] instances must be
   * strictly evaluated to keep track of the order of them.
   * In other words, they must be defined as `val`.
   */
  trait DirtyEnum extends Enum {
    protected override type Elem <: DirtyElem

    protected final override def element(ordinal: Int): Elem = values(ordinal)
    protected final override def ordinal(element: Elem): Int = element.ordinal

    protected trait DirtyElem {
      val ordinal = values.size
      values += this.asInstanceOf[Elem] //Wow XD
    }

    private val values = collection.mutable.Buffer[Elem]()
  }
}
