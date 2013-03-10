/**
 *
 */
package com.idyria.aib.core.service

import scala.beans.BeanProperty
import com.idyria.aib.core.bus.aib

/**
 * @author rleys
 *
 */
abstract class Service (
    
    /**
     * Name of this service
     */
    @BeanProperty
    protected var name: String ) extends ServiceLifecycle {

  
  // Register in aib
  aib register(this)
  
  /**
   * Parent service of this service
   */
  protected var parent: Service = null

  /**
   * Children of this service
   */
  protected var children = scala.collection.immutable.Set[Service]()

  

  
  
  
  
  /**
   * Parent setter
   * 
   */
  def setParent(service:Service) = parent = service
  
}