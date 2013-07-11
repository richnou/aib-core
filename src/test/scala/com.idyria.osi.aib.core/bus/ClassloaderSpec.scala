/**
 *
 */
package com.idyria.osi.aib.core.bus

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FeatureSpec
import java.net.URLClassLoader
import java.net.URL
import org.scalatest.GivenWhenThen
import java.util.concurrent.CyclicBarrier

/**
 * @author rleys
 *
 */
class ClassloaderSpec extends FeatureSpec
					   	with BeforeAndAfterAll
						with GivenWhenThen
						with AIBTestEvents {


  feature("Classloader virtualisation") {

    info("Each Classloader instance should map to a separate bus")

    scenario("2 AIB in 2 brother classloader") {

      // Create Two Classloaders
      //--------------------------
      Given("Two Classloaders sharing same parent")
      var cl1 = URLClassLoader.newInstance(Array[URL]())
      var cl2 = URLClassLoader.newInstance(Array[URL]())

      When("Asking aib for their bus instance")
      var aibInstances = Map[aib,Int]()
      var runnable = new Runnable() {
        def run() {

          var taib = aib.getBus
          // Get AIB Bus and record count  into map
          synchronized {
	          var oldCount = aibInstances.contains(taib) match {
	            case true => aibInstances(taib)
	            case false => 0
	          }
	          aibInstances += (taib -> (oldCount+1))
          }
          println("Returned aib instance: "+taib.hashCode())

          Thread.currentThread().interrupt()

        }
      }
      var th1 = new Thread(runnable)
      th1.setContextClassLoader(cl1)
      var th2 = new Thread(runnable)
      th2.setContextClassLoader(cl2)


      Then("each thread must map to two different instances of aib bus")
      th1.start()
      th2.start()
      th1.join
      th2.join

      // Verify map entries all are 1
      assert {
        aibInstances.values.forall( c => c==1) ===true
      }

    }

    scenario("2 AIB in 2 parented classloader") {

      Given("Two Classloaders with one child and one parent")
      var cl1 = URLClassLoader.newInstance(Array[URL](),Thread.currentThread().getContextClassLoader())
      var cl2 = Thread.currentThread().getContextClassLoader()

      When("Asking aib for their bus instance")
      var aibInstances = Map[aib,Int]()
      var runnable = new Runnable() {
        def run() {


          var taib = aib.getBus
          synchronized {

	          // Get AIB Bus and record count  into map
	          var oldCount = aibInstances.contains(taib) match {
	            case true => aibInstances(taib)
	            case false => 0
	          }
	          aibInstances += (taib -> (oldCount+1))
          }

          println("Returned aib instance: "+taib.hashCode())

          Thread.currentThread().interrupt()

        }
      }
      var th1 = new Thread(runnable)
      th1.setContextClassLoader(cl1)
      var th2 = new Thread(runnable)
      th2.setContextClassLoader(cl2)


      Then("each thread must map to two different instances of aib bus")
      th1.start()
      th2.start()
      th1.join
      th2.join

      // Verify map entries all are 1
      assert {
        aibInstances.values.forall( c => c==1) ===true
      }

    }

  }


  feature("Events Isolation") {

    info("Verify the event send operators are effectively virtualised")

    scenario("Same events and listeners land in two busses") {

      //-- Concurrent Logic
      Given("Two Classloaders sharing same parent")
      var sharedCounter = 0
      var cl = {
        event : EventOne =>

          println("Executing Event")
          synchronized {
            sharedCounter += 1
          }

      }

      //-- Threads
      var initBarrier = new CyclicBarrier(2)
      var threads = for (i <- 0 to 1) yield {
        var th = new Thread() {

          val threadId = i

          override def run() = {

            // Register closure definition
            aib registerClosure(cl)

            // Barrier
            initBarrier.await()

            // Thread 0 only Sends an event
            println("Iam Thread "+threadId)
            if (threadId==0)
            	aib ! new EventOne()

            Thread.sleep(100)

          }
        }
        th.setContextClassLoader(URLClassLoader.newInstance(Array[URL]()))
        th
      }

      //-- Start and join
      threads.foreach {thread => thread.start}
      threads.foreach {thread => thread.join}

      //-- Only thread 0 send an event, so the counter must be 1
      assert(sharedCounter === 1)
    }



  }



}
