/**
 *
 */
package com.idyria.osi.aib.core.bus

import org.scalatest.FunSuite
import scala.collection.mutable.Stack

/**
 * @author rleys
 *
 */
class DispatchSpec extends FunSuite {


  test("pop is invoked on a non-empty stack") {

    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    val oldSize = stack.size
    val result = stack.pop()
    assert(result === 2)
    assert(stack.size === oldSize - 1)
  }

}
