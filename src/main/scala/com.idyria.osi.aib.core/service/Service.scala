/**
 *
 */
package com.idyria.osi.aib.core.service


import scala.beans.BeanProperty
import com.idyria.osi.aib.core.bus.aib

/**
 * @author rleys
 *
 */
abstract class Service (

    /**
     * Name of this service
     */
     @BeanProperty
    var name: String ) extends ServiceLifecycle {


  // Register in aib
  aib register(this)

  /**
   * Parent service of this service
   */
  var parent: Service = null

  /**
   * Children of this service
   */
  var children = scala.collection.immutable.Set[Service]()


  /**
   * Parent setter
   *
   */
  def setParent(service:Service) = parent = service

}
