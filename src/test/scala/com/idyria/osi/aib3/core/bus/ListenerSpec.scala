/**
 *
 */
package com.idyria.osi.aib3.core.bus

import org.scalatest.FeatureSpec
import com.idyria.aib3.core.bus.AIBEvent
import com.idyria.aib3.core.bus.aib
import org.scalatest.BeforeAndAfterAll

/**
 * @author rleys
 *
 */
class ListenerSpec extends FeatureSpec with BeforeAndAfterAll {

  // Define events
  class EventOne extends AIBEvent {

  }

  def before {
    
    //aib doStart
    
  }
  
  def after {
    
    aib.doStop
    aib clean
    
  }
  
  feature("Register Listener") {

    scenario("Listener is an Object") {

      //-- Register object
      object firstObject {

        def echo(x: EventOne): Unit = {

        }

        def echo2(cl: EventOne => Unit) {

        }

      }
      aib.register(firstObject)

      //-- Verify we have one listener detected
      assert(aib.getListeners.size===1)
     
      
    }

    scenario("Listener is a closure")(pending)

  }

}