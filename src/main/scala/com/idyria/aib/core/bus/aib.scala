/**
 *
 */
package com.idyria.aib.core.bus

import java.lang.reflect.Method
import scala.Array.canBuildFrom
import scala.actors.Actor
import scala.actors.Reactor
import scala.beans.BeanProperty
import com.idyria.aib.core.bus.AIBEvent
import scala.actors.scheduler.ExecutorScheduler

/**
 * @author rleys
 *
 */
class aib extends Actor with AIBEventDispatcher {

  /**
   * Maps all the events type to possible listeners
   */
  protected var listeners = scala.collection.mutable.Map[Class[_], scala.collection.mutable.Set[AIBEventListener]]()
 
  /**
   * This embedded class wraps an event receiver around a reacting actor
   * This actor checks is the reference to the closure is still available (not Collected),
   * and marks itself as invalid if it is the case, so that it can be cleaned later
   */
  class ListenerActor(var closure: (AnyRef => Unit)) extends Reactor[Any] with AIBEventListener {

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
      	case "exit" => exit
        case event: AnyRef => {
          //println(s"Executing closure with event ${event.getClass} on $target, method is ${closure.getName()}")
          
          closure(event)
          
         //var args = Array[Object](event: _*)
          /*val params: List[Object] = List(event)
          closure.invoke(target,params: _*); */
       
          act
        }
        
      }

    }

    /**
     * Sends the aib exit string to the actor so that the act method will exit
     */
    def stop = this ! "exit"
    
    def dispatch(msg: Any) = this ! msg
    
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
      case event: Any => {
        println("Got AIBEvent: " + event.getClass)

        // Send to all Actors, and clean at the same time
        //--------
        if (this.listeners.contains(event.getClass)) {

          println("-- Dispatching")

          // Clean valids
          this.listeners(event.getClass) = this.listeners(event.getClass).filter(listener => listener.valid)

          // Dispatch message
          this.listeners(event.getClass).foreach(listener => listener dispatch (event)) 
        }

        act

      }
      //case x if(classOf[AIBEvent].isAssignableFrom(x.getClass)) => println("Got AIBEvent")
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
    if (!this.listeners.contains(eventType))
      this.listeners(eventType) = scala.collection.mutable.Set[AIBEventListener](new ListenerActor(cl))
    else
      this.listeners(eventType) += new ListenerActor(cl)
    
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
    if (!this.listeners.contains(eventType))
      this.listeners(eventType) = scala.collection.mutable.Set[AIBEventListener](new ListenerActor(cl.asInstanceOf[Any=>Unit]))
    else
      this.listeners(eventType) += new ListenerActor(cl.asInstanceOf[Any=>Unit])
    
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
      case x if (x.getParameterTypes().length == 1 && classOf[Any].isAssignableFrom(x.getParameterTypes()(0)) && x.getAnnotation(classOf[EventCatcher])!=null) => x
    } foreach {
      m =>
        println(s"Found Method: $m")

        this.registerCatcher(listener,m)
        
        
        
        // Create Closure Type
        //---------
        /*var cl: (Any) => Unit = {
          ev: Any => val r = m.invoke(listener, ev)
        }
        
       // eventClassType.
        
        var cl2: ( Class[Any], Any ) => Unit = {
          (cl : Class[_],ev : Any)  => val r = m.invoke(listener, ev)
        }
        
        var closureMethod = cl2.getClass().getMethods().filter {
	      m => 
	        	//println(s"Method detected: $m"); 
	        	m.getName() == "apply" && m.getReturnType() == Void.TYPE
	    }.foreach {
	      m => 
	        println(s"Method detected: $m");
	        var t = m.getParameterTypes()(0)
	        println("Will be Registering Closure event type: " + t.getTypeParameters()(0))
	    }
        //println("Will be Registering Closure event type: " + (closureMethod.getGenericParameterTypes()(0)))

        // Register It
        //----------------
        this.registerClosure(cl)*/
    }

  }
  
  def getListeners : scala.collection.mutable.Map[Class[_], scala.collection.mutable.Set[AIBEventListener]] = listeners

}

object aib extends AIBEventDispatcher {

  
  
  
  /**
   * Real busses instances mapped to classloaders
   */
  var busses = Map[ClassLoader, aib]()

  def doStop = getBus doStop

  /**
   * Default sends a message to Classloader bus
   */
  def !(msg: Any) = getBus ! msg
  
  /**
   * Sends a transversal message to all buses
   */
  def <-!-> (msg: Any) = {
    
    synchronized {
      this.busses.values.foreach {
        bus => bus ! msg
      }
    }
    
  }
  
  def register(listener: AnyRef) = getBus register (listener)

  def registerClosure[ T <: AnyRef](cl: T => Unit) = getBus registerClosure (cl)

  def getListeners : scala.collection.mutable.Map[Class[_], scala.collection.mutable.Set[AIBEventListener]] = {
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