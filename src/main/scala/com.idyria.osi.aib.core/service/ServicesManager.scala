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


    topServices.foreach(_.aibInit)


  }


  override def aibStart = {

    topServices.foreach(_.aibStart)

  }


  override def aibSuspend = {

  }

  override def aibResume = {

  }


  override def aibStop = {

  }


}
