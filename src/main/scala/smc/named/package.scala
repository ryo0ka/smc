package smc

/**
  * Adds utilities to a tuple type [[(String, A)]] naming it [[smc.named.Named]].
  *
  * {{{
  *   import smc.named._
  *
  *   type Foo[A] = List[(String, A)]
  *   type Foo[A] = List[Named[A]]
  *
  *   val a = ("my birth year", 2015)
  *   val a = name(2015, "my birth year")
  *   val a = 2015 smc.named "my birth year"
  *
  *   val b = a._1
  *   val b = a.name
  *
  *   val c = a._2
  *   val c = a.value
  * }}}
  */
package object named {
  /**
    * [[Tuple2]] where the first type is [[String]].
    */
  type Named[+A] = (String, A)

  /**
    * Constructs a [[Named]] with a given name and value.
    */
  def name[A](value: A, name: String): Named[A] = (name, value)

  /**
    * Provides additional methods to any type of values to construct [[Named]] from.
    */
  implicit class FromVal[+A](val value: A) extends AnyVal {
    /**
      * Names the value.
      */
    def named(name: String): Named[A] = (name, value)
  }

  /**
    * Provides additional methods to [[Named]] instances.
    */
  implicit class NamedOps[+A](val named: Named[A]) extends AnyVal {
    /**
      * Retrieves the name.
      */
    def name: String = named._1

    /**
      * Retrieves the value.
      */
    def value: A = named._2
  }

}
