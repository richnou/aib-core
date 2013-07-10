/**
 *
 */
package com.idyria.osi.aib.core.dependencies

/**
 *
 * Trait for any class that can provide enough information to be seen as an artifact to be resolved
 * @author rleys
 *
 */
trait ArtifactTrait[T] {


  var artifactId : T


  /*def getArtifactId : T

  def getGroupId : T

  def getVersion : T

  def getPackaging : T*/


  /**
   * true: The Artifact is resolved and has full description available, false if it only entails its base parameters (artifactId,groupId,version)
   */
  var resolved = false

}
