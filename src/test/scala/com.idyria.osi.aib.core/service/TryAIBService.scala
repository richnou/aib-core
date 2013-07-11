package com.idyria.osi.aib.core.service



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
