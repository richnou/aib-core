/**
 *
 */
package com.idyria.osi.aib.core.service

/**
 * @author rleys
 *
 */
trait ServiceLifecycle {


  /**
   * Init step
   */
  def aibInit = {}

  /**
   * Start
   */
  def aibStart = {}

  /**
   * Put object in standby
   */
  def aibSuspend = {}

  /**
   * Resume after standby
   */
  def aibResume = {}

  /**
   * Completely finish
   */
  def aibStop = {}

}
