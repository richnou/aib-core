/**
 *
 */
package com.idyria.aib3.core.bus

import scala.Array.canBuildFrom
import scala.actors.Actor
import com.idyria.aib3.core.bus.AIBEvent
import scala.actors.Reactor
import scala.beans.BeanProperty

/**
 * @author rleys
 *
 */
class aib extends Actor with AIBEventDispatcher {

  /**
   * Maps all the events type to possible listeners
   */
  protected var listeners = scala.collection.mutable.Map[Class[_ <: AIBEvent], scala.collection.mutable.Set[AIBEventListener[_]]]()
 
  /**
   * This embedded class wraps an event receiver around a reacting actor
   * This actor checks is the reference to the closure is still available (not Collected),
   * and marks itself as invalid if it is the case, so that it can be cleaned later
   */
  class ListenerActor[T <: AIBEvent](var closure: (T => Unit)) extends Reactor[Any] with AIBEventListener[T] {

    /**
     * false if the reference to the closure is gone, and the actor can be flushed
     */
    var valid = true

    // Always start
    start()

    /**
     * Receives an event:
     *
     *  - Verify the closure reference is still valid
     *  - Mark itself as invalid if necessary
     *  - Execute if valid
     */
    def act() {

      react {

        case event: T => closure(event); act
        case "exit" => exit
      }

    }

    def stop = this ! "exit"
    
    def dispatch(msg: AIBEvent) = this ! msg 

  }
  
  // Always start
  start()

  /**
   * Send ourselves a stop event
   */
  def doStop = {

    this ! "exit"

  }
 

  /**
   * Act reacts on events
   */
  def act() {

    println("-- AIB act --")
    react {

      // Exit:
      // - Stop remaining actors
      // - Exit
      case "exit" => {

        println("Exiting")

        // Clean
        this.listeners.values.foreach { actors => actors.foreach { actor => actor.stop } }

        // Exit
        exit
      }
      case x: String => println(s"Got String: $x"); act
      case event: AIBEvent => {
        println("Got AIBEvent: " + event.getClass)

        // Send to all Actors, and clean at the same time
        //--------
        if (this.listeners.contains(event.getClass)) {

          println("-- Dispatching")

          // Clean valids
          this.listeners(event.getClass) = this.listeners(event.getClass).filter(listener => listener.valid)

          // Dispatch message
          this.listeners(event.getClass).foreach(listener => listener dispatch event)
        }

        act

      }
      //case x if(classOf[AIBEvent].isAssignableFrom(x.getClass)) => println("Got AIBEvent")
    }

  }

  /**
   * Registers a single anonymous closure
   */
  def registerClosure[T <: AIBEvent](cl: T => Unit) = {

    // Find real type of T: This is the apply(T) : void method
    //-----------
    var closureMethod = cl.getClass().getMethods() filter {
      m => m.getName() == "apply" && m.getReturnType() == Void.TYPE
    } head
    var eventType = (closureMethod.getParameterTypes()(0).asInstanceOf[Class[AIBEvent]])
    println("Registering Closure event type: " + eventType)

    // Map It
    //---------------
    if (!this.listeners.contains(eventType))
      this.listeners(eventType) = scala.collection.mutable.Set[AIBEventListener[_]](new ListenerActor[T](cl))
    else
      this.listeners(eventType) += new ListenerActor[T](cl)
  }

  /**
   * This Register method scans a complete object vor available Event listener definitions
   */
  def register(listener: AnyRef) = {

    println("Registering Object" + listener.getClass())

    // Looking for method receiving an event
    //--------------
    listener.getClass().getMethods().collect {

      //case x if(x.getParameterTypes().length==1 &&  AIBEvent.getClass().isAssignableFrom(x.getParameterTypes()(0))) => x
      //case x if(x.getParameterTypes().length==1 &&  AIBEvent.getClass().isInstance(x.getParameterTypes()(0))) => x
      case x if (x.getParameterTypes().length == 1 && classOf[AIBEvent].isAssignableFrom(x.getParameterTypes()(0))) => x
    } foreach {
      m =>
        println(s"Found Method: $m")

        // Create Closure Type
        //---------
        var cl: (AIBEvent) => Unit = {
          ev: AIBEvent => val r = m.invoke(listener, ev)
        }

        // Register It
        //----------------
        this.registerClosure(cl)
    }

  }
  
  def getListeners : scala.collection.mutable.Map[Class[_ <: AIBEvent], scala.collection.mutable.Set[AIBEventListener[_]]] = listeners

}

object aib extends AIBEventDispatcher {

  /**
   * Real busses instances mapped to classloaders
   */
  var busses = Map[ClassLoader, aib]()

  def doStop = getBus doStop

  def !(msg: Any) = getBus ! msg
  
  def register(listener: AnyRef) = getBus register (listener)

  def registerClosure[T <: AIBEvent](cl: T => Unit) = getBus registerClosure (cl)

  def getListeners : scala.collection.mutable.Map[Class[_ <: AIBEvent], scala.collection.mutable.Set[AIBEventListener[_]]] = {
    getBus getListeners
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
	      case true => busses(cl)
	      case false => { 
	    	  busses += (cl -> new aib()); 
	    	  busses(cl) 
		  }
	    }
    }

  }

}