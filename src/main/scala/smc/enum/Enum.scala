package smc.enum

trait Enum {
  /**
    * Defines the type of elements to map to/from numbers.
    */
  protected type EnumElement

  /**
    * Maps a given integer to a corresponding element.
    *
    * @param ordinal a number to search the corresponding element for.
    * @return an element that is mapped to the given number.
    * @throws NoSuchElementException or whatever exceptions if no elements are mapped to the given number.
    */
  protected def element(ordinal: Int): EnumElement

  /**
    * Maps a given element to a corresponding number.
    *
    * @param element an element to search the corresponding number for.
    * @return a number that is mapped to the given element.
    * @throws NoSuchElementException or whatever exceptions if no numbers are mapped to the given element.
    */
  protected def ordinal(element: EnumElement): Int
}