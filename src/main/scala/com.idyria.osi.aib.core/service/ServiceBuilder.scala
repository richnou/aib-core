/**
 *
 */
package com.idyria.osi.aib.core.service

import scala.collection.mutable.Stack

import scala.language.implicitConversions

/**
 *
 * The service Builder allows DSL construction of services hierarchy using this syntax:
 *
 * new ServiceGroup("top") {
 *
 * 	service {
 *
 *     group {
 *
 *     	  service "AnotherOne" {
 *
 *        }
 *
 *        service {
 *
 *        }
 *
 *     }
 *
 *  }
 *
 *
 * }
 *
 *
 *
 * @author rleys
 *
 */
class ServiceBuilder {

  /**
   * Stores all the top services
   */
  var topServices = Set[Service]()

  var servicesStack = Stack[Service]()

  /**
   * A Helper class whose lifecycle method can be defined externally using closures setters
   */
  class BuilderService(name: String) extends Service(name) {

    var aibInitClosure: (() => Unit) = { () => }
    var aibStartClosure: (() => Unit) = { () => }
    var aibSuspendClosure: (() => Unit) = { () => }
    var aibResumeClosure: (() => Unit) = { () => }
    var aibStopClosure: (() => Unit) = { () => }

    def onInit(cl: => Unit) = this.aibInitClosure = { () => cl }
    def onStart(cl: => Unit) = this.aibStartClosure = { () =>  cl }
    def onSuspend(cl: => Unit) = this.aibSuspendClosure = { () => cl }
    def onResume(cl: => Unit) = this.aibResumeClosure = { () => cl }
    def onStop(cl: => Unit) = this.aibStopClosure = { () => cl }

    override def aibInit = aibInitClosure()
    override def aibStart = aibStartClosure()
    override def aibSuspend = aibSuspendClosure()
    override def aibResume = aibResumeClosure()
    override def aibStop = aibStopClosure()

  }

  protected def createService(name: String): BuilderService = {

    // Create Service
    //------------
    var newService = new BuilderService(name)

    newService
  }

  def service(cl: BuilderService => Unit): Service = service(s"Unnamed ${servicesStack.size}")(cl)
  def service(name: String)(cl: BuilderService => Unit): BuilderService = {

    // Create Service
    //------------
    var newService = this.createService(name)

    // Call in Content
    //-------------
    serviceWith(newService)(cl)

    newService

  }

  def serviceWith[T <: Service](service: T)(cl: T => Unit): T = {

    // println("Adding Service Instance to ServiceBuilder")

    //-- Add to top services oder parent service
    //----------------------------
    if (servicesStack.size > 0)
      service.setParent(servicesStack.head)
    else
      topServices += service

    // Add to stack head
    //-------------
    servicesStack.push(service)
    var res = cl(service)
    servicesStack.pop()

    service

  }

  def service[T <: Service](service: T): T = this.serviceWith(service) { s => }

}

object ServiceBuilder {

}
