/**
 *
 */
package com.idyria.osi.aib.core.bus

import java.lang.reflect.Method
import scala.Array.canBuildFrom
import scala.beans.BeanProperty


import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging


/**
 * @author rleys
 *
 */
class aib (

    var name : String = null

  )  extends AIBEventDispatcher {

  // Create Actor System
  //----------------------------------

  //-- Take name of create
  if (name==null) {
    name = "default-"+this.hashCode()
  }

  //-- Create System
  val system = ActorSystem(name)


  /**
   * Maps all the events type to possible listeners
   */
  protected var listeners = scala.collection.mutable.Map[Class[_], scala.collection.mutable.Set[ActorRef]]()


  // Actor for a listener
  //----------------------------------

  /**
   * This embedded class wraps an event receiver around a reacting actor
   * This actor checks if the reference to the closure is still available (not Collected),
   * and marks itself as invalid if it is the case, so that it can be cleaned later
   */
  class ListenerActor(var closure: (AnyRef => Unit)) extends Actor with AIBEventListener {

    /**
     * false if the reference to the closure is gone, and the actor can be flushed
     */
    var valid = true

    // Always start
    //start()

    /**
     * Receives an event:
     *
     *  - Verify the closure reference is still valid
     *  - Mark itself as invalid if necessary
     *  - Execute if valid
     */
    def receive =  {

    	//case "exit" => exit
      case event : AnyRef => {
        //println(s"Executing closure with event ${event.getClass} on $target, method is ${closure.getName()}")

        closure(event)

       //var args = Array[Object](event: _*)
        /*val params: List[Object] = List(event)
        closure.invoke(target,params: _*); */

      }


    }

    /**
     * Sends the aib exit string to the actor so that the act method will exit
     */


    /**
      Not doing anything for API because of API compatibility
    */
    def dispatch(msg: Any) = {

    }

    def stop = {
        this.valid = false
    }

  }


  /**
   * Send ourselves a stop event
   */
  def doStop = {

    this send "exit"

  }


  /**
   * Event send (syntax is like Actors)
   */
  def send (event: Any) = {

      // Send to all Actors, and clean at the same time
      //--------
      if (this.listeners.contains(event.getClass)) {

        println("-- Dispatching")

        // Clean valids
        //this.listeners(event.getClass) = this.listeners(event.getClass).filter(listener => listener.asInstanceOf[AIBEventListener].valid)

        // Dispatch message
        this.listeners(event.getClass).foreach(listener => listener ! (event))
      }

  }



  def registerCatcher(listener: AnyRef,method: Method) = {

    var eventType = method.getParameterTypes()(0)
    //println(s"Found event class type: ")
    println(s"Registering event catcher for: $eventType")

    // Create Closure Type
    //---------
    var cl: (AnyRef) => Unit = {
      ev: AnyRef => val r = method.invoke(listener, ev)
    }

    // Map It
    //---------------

    //-- Create Actor
    var listenerActor = system.actorOf(Props(new ListenerActor(cl)))

    //-- Map
    if (!this.listeners.contains(eventType))
      this.listeners(eventType) = scala.collection.mutable.Set[ActorRef](listenerActor)
    else
      this.listeners(eventType) += listenerActor

  }

  /**
   * Registers a single anonymous closure
   */
  def registerClosure[ T <: AnyRef](cl: T => Unit) = {

    // Find real type of T: This is the apply(T) : void method
    //-----------
    var closureMethod = cl.getClass().getMethods().filter {
      m => m.getName() == "apply" && m.getReturnType() == Void.TYPE
    }.head

    var eventType = (closureMethod.getParameterTypes()(0).asInstanceOf[Class[AIBEvent]])
    println("Registering Closure event type: " + eventType)
    println("Registering Closure event type: " + (closureMethod.getParameterTypes()(0)))

    //var clref : (AnyRef => Unit)

    // Map It
    //---------------

    //-- Create Actor
    var listenerActor = system.actorOf(Props(new ListenerActor(cl.asInstanceOf[Any=>Unit])))

    if (!this.listeners.contains(eventType))
      this.listeners(eventType) = scala.collection.mutable.Set[ActorRef](listenerActor)
    else
      this.listeners(eventType) += listenerActor

  }

  /**
   * This Register method scans a complete object for available Event listener definitions
   */
  def register(listener: AnyRef) = {

    println("Registering Object" + listener.getClass())

    // Looking for method receiving an event
    //--------------
    listener.getClass().getMethods().collect {

      //case x if(x.getParameterTypes().length==1 &&  AIBEvent.getClass().isAssignableFrom(x.getParameterTypes()(0))) => x
      //case x if(x.getParameterTypes().length==1 &&  AIBEvent.getClass().isInstance(x.getParameterTypes()(0))) => x
      case x if (x.getParameterTypes().length == 1 && classOf[AIBEvent].isAssignableFrom(x.getParameterTypes()(0))) => x
      case x if (x.getParameterTypes().length == 1 && classOf[Any].isAssignableFrom(x.getParameterTypes()(0)) && x.getAnnotation(classOf[EventCatcher])!=null) => x
    } foreach {
      m =>
        println(s"Found Method: $m")

        this.registerCatcher(listener,m)

    }

  }

  /**

    Convert the ActorRefs in the source map, to AIBEventListener

  */
  def getListeners :  scala.collection.Map[Class[_],  scala.collection.Set[AIBEventListener]] = {

    /*listeners.map {

      case(eventClass,actorListeners) => ( eventClass, actorListeners.map {_.asInstanceOf[AIBEventListener]} )

    }*/
    listeners.mapValues { _.map {_.asInstanceOf[AIBEventListener]}}

  }

}

/**

  aib singleton used as entry point

*/
object aib extends AIBEventDispatcher {

  // Create actor System
  //---------------------------

  val system = ActorSystem("AIBSystem")


  /**
   * Real busses instances mapped to classloaders
   */
  var busses = Map[ClassLoader, aib]()

  /// Stop the local bus
  def doStop = getBus.doStop

  /**
   * Default sends a message to local bus
   */
  def !(msg: Any) = getBus send msg
  def send (msg: Any) = getBus send msg

  /**
   * Sends a transversal message to all buses
   */
  def <-!-> (msg: Any) = {

    synchronized {
      this.busses.values.foreach {
        bus => bus send msg
      }
    }

  }

  /// Register a new listener object to the local bus
  def register(listener: AnyRef) = getBus register (listener)

  /// Register a new Closure listener to the local bus
  def registerClosure[ T <: AnyRef](cl: T => Unit) = getBus registerClosure (cl)

  /// @return All the listeners registered in local bus
  def getListeners :  scala.collection.Map[Class[_],  scala.collection.Set[AIBEventListener]] = {
      getBus.getListeners
  }

  /**
   * Clean all the busses for all Classloaders
   */
  def clean = {

    busses.empty

  }

  /**
   * Returns the aib bus of the current Thread
   */
  def getBus: aib = {

    var cl = Thread.currentThread().getContextClassLoader()
    synchronized {
	    busses.contains(cl) match {
	      case true => busses(cl).asInstanceOf[aib]
	      case false => {

          // Create AIB Bus actor
          var aibActor =  new aib

          // Record into busses map
	    	  busses += (cl -> aibActor);
	    	  aibActor
		  }
	    }
    }

  }



}
