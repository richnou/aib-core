/**
 *
 */
package com.idyria.osi.aib.core.service

import scala.collection.mutable.Stack

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
  class BuilderService(name : String) extends Service(name) {

    var aibInitClosure: (Unit => Unit) = null
    var aibStartClosure: ( Unit => Unit) = null
    var aibSuspendClosure: (Unit => Unit) = null
    var aibResumeClosure: (Unit => Unit) = null
    var aibStopClosure: (Unit => Unit) = null


    override def aibInit = callClosure(aibInitClosure)
    override def aibStart = callClosure(aibStartClosure)
    override def aibSuspend = callClosure(aibSuspendClosure)
    override def aibResume = callClosure(aibResumeClosure)
    override def aibStop = callClosure(aibStopClosure)

    def callClosure(cl: => Unit) = cl match { case x if (x != null) => x }

  }

  protected def createService(name: String): BuilderService = {

    // Create Service
    //------------
    var newService = new BuilderService(name)

    //-- Add to top of stack
    if (servicesStack.size > 0)
      newService.setParent(servicesStack.head)
    else
      topServices += newService
    newService
  }

  def service(cl: BuilderService => Unit): Service = service(s"Unnamed ${servicesStack.size}")(cl)
  def service(name: String)(cl: BuilderService => Unit): BuilderService = {

    // Create Service
    //------------
    var newService = this.createService(name)

    // Call in Content
    //-------------
    service(newService)(cl)

  }

  def service[T <: Service](service: T)(cl: (T) => Unit): T = {

    // Call in Content
    //-------------
    servicesStack.push(service)
    var res = cl(service)
    servicesStack.pop()

    service

  }

}
