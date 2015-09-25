/**
 *
 */
package com.idyria.osi.aib.core.bus

/**
 *
 * This trait defines the methods defined by an event dispatcher
 * @author rleys
 *
 */
trait AIBEventDispatcher {


  /**
   * Simply stops the dispatcher
   */
  def doStop

  /**
   * Event send (syntax is like Actors)
   */
  def send (msg: Any)

  /**
   * Scans a reference for methods that can received an Event
   */
  def register(listener: AnyRef)

  /**
   * Registers a closure to be activated upon receiving a specific event type
   */
  def registerClosure[ T <: AnyRef](cl: T => Unit)

  def getListeners : scala.collection.Map[Class[_], scala.collection.Set[AIBEventListener]]

}
