/**
 *
 */
package com.idyria.osi.aib.core.dependencies.maven.model

import com.idyria.osi.ooxoo.core.buffers.structural._
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer
import com.idyria.osi.aib.core.dependencies.ArtifactTrait
import java.io.File
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import java.io.FileInputStream

/**
 * @author rleys
 *
 */
@xelement(name = "project",ns="http://maven.apache.org/POM/4.0.0")
class Project extends ElementBuffer {

  @xelement(ns="http://maven.apache.org/POM/4.0.0")
  var artifactId: XSDStringBuffer = null
  
  @xelement(ns="http://maven.apache.org/POM/4.0.0")
  var groupId: XSDStringBuffer = null
  
  @xelement(ns="http://maven.apache.org/POM/4.0.0")
  var version: XSDStringBuffer = null
  
  @xelement(ns="http://maven.apache.org/POM/4.0.0")
  var packaging : XSDStringBuffer = null
  

  /**
   * <Dependencies>
   */
  @xelement(name = "dependencies")
  var dependencies: XList[Dependencies] = XList[Dependencies] { new Dependencies }

}
object Project {

  /**
   * Try to find a project definition from:
   *
   * Classloader META-INF/maven/groupId/artifactId/pom.xml
   * Local file start: pom.xml
   *
   */
  def apply(groupId: String, artifactId: String): Option[Project] = {

    // Look for possibilities
    var url = Thread.currentThread().getContextClassLoader().getResource(s"META-INF/maven/$groupId/$artifactId/pom.xml")
    var file = new File("pom.xml")

    // Resolve
    (url, file.exists()) match {
      case (null, false) => None

      // Use URL
      case (url, false)  =>
      	
        var project = new Project()
        project.appendBuffer(new StAXIOBuffer(url.openStream()))
        project.lastBuffer.streamIn
        
        Some(project)
        
      // Use File
      case (_, true)  =>
        
        var project = new Project()
        project.appendBuffer(new StAXIOBuffer(new FileInputStream(file)))
        project.lastBuffer.streamIn
        
        Some(project)
        
    }


  }

}

@xelement(name = "dependencies")
class Dependencies extends ElementBuffer {

  /**
   * <Dependency>
   */
  @xelement(name = "dependency")
  var dependency = XList[Dependency] { new Dependency }

}

@xelement(name = "dependency")
class Dependency extends ElementBuffer with ArtifactTrait[XSDStringBuffer] {

  //
  var artifactId: XSDStringBuffer = null

  /*var groupId: XSDStringBuffer
  var version: XSDStringBuffer
  var packaging : XSDStringBuffer*/

}
