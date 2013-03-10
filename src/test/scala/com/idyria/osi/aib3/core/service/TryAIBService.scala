package com.idyria.osi.aib3.core.service

import com.idyria.aib.core.service.ServiceGroup
import com.idyria.aib.core.service.ServiceBuilder
import com.idyria.aib.core.service.ServicesManager

object TryAIBService extends App {

  println("Trying Service Builder")
  
  
  object services extends ServicesManager {
    
    service("A Service") { s=>
      
      service("Sub a") { s => }
        
    }
    
    service("Another Service") { s=>
      
      
      service("Sub b") { s => }
      
    }
    
   
    
    
    
    
  }
  
 
  
  // Print back services
  services.topServices.foreach {
    s => println("Service: "+s.getName)
  }
  
  
  
}