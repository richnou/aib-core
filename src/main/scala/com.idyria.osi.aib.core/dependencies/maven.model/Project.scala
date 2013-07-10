/**
 *
 */
package com.idyria.osi.aib.core.dependencies.maven.model

import com.idyria.osi.ooxoo.core.buffers.structural._
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer
import com.idyria.osi.aib.core.dependencies.ArtifactTrait


/**
 * @author rleys
 *
 */
@xelement("Project")
class Project extends ElementBuffer  {

  /*var artifactId: XSDStringBuffer
  var groupId: XSDStringBuffer
  var version: XSDStringBuffer
  var packaging : XSDStringBuffer*/

  /**
   * <Dependencies>
   */
  @xelement("Dependencies")
  var dependencies: XList[Dependencies] =  XList[Dependencies] { new Dependencies }

}

class Dependencies extends ElementBuffer {

  /**
   * <Dependency>
   */
  @xelement("Dependency")
  var dependency = XList[Dependency] { new Dependency }

}

class Dependency extends ElementBuffer  with ArtifactTrait[XSDStringBuffer] {

  //
  var artifactId: XSDStringBuffer = null

  /*var groupId: XSDStringBuffer
  var version: XSDStringBuffer
  var packaging : XSDStringBuffer*/

}
