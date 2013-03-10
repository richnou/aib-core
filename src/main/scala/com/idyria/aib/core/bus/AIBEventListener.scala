/**
 *
 */
package com.idyria.aib.core.bus

/**
 * @author rleys
 *
 */
trait AIBEventListener {

  /**
   * Cleans out this listener
   */
  def stop

  /**
   * @return true if the listener is valid
   */
  def valid: Boolean

  /**
   * Dispatch an event to this listener
   */
  def dispatch(msg: Any)

  
}