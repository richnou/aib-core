package com.idyria.osi.aib3.core.bus

import com.idyria.aib.core.bus.AIBEvent
import com.idyria.aib.core.bus.aib

object TryAIBScala extends App {

  
  // Define events
  class EventOne extends AIBEvent  {
    
  }
  
  // Register Listeners from different class loaders
  //--------------
   
  // Type A
  object firstObject {
    
     
    def echo(x: EventOne) : Unit = {
      
    }
    
    def echo2(cl: EventOne => Unit) {
      
    }
    
  }
  
  
  aib register firstObject
  
  object secondObject {
    
    
  }
  
  var cl =  {
    
    ev : EventOne =>
      
      println("Executing anonymous closure")
    
  }
   
  aib.registerClosure(cl)   
    
  // Type B
  
  
  // Send messages to see
  //--------------------------
  aib ! "Hello!"
  aib ! new EventOne()
  aib ! "Hello2!"
  
  
  aib ! "exit"
  
  
}