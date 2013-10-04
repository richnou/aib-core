/**
 *
 */
package com.idyria.osi.aib.core.service

/**
 * This services manager is a service builder that records the top services and provides basic facilities for managing the created services
 *
 *
 *
 *
 * @author rleys
 *
 */
class ServicesManager extends ServiceBuilder with ServiceLifecycle {


  /**
   * Inits all the services
   */
  override def aibInit = {

    
    topServices.foreach {
      s => 
        println(s"[Service] init on ${s.name}")
        s.aibInit
    }


  }


  override def aibStart = {

    topServices.foreach {
      s => 
        println(s"[Service] start on ${s.name}")
        s.aibStart
    }

  }


  override def aibSuspend = {

    topServices.foreach {
      s => 
        println(s"[Service] suspend on ${s.name}")
        s.aibSuspend
    }
    
  }

  override def aibResume = {

    topServices.foreach {
      s => 
        println(s"[Service] resume on ${s.name}")
        s.aibResume
    }
    
  }


  override def aibStop = {

    topServices.foreach {
      s => 
        println(s"[Service] stop on ${s.name}")
        s.aibStop
    }
    
  }


}
