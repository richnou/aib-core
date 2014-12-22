/**
 *
 */
package com.idyria.osi.aib.core.dependencies

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FeatureSpec
import scala.io.Source
import com.idyria.osi.aib.core.dependencies.maven.model.Project
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import java.io.InputStreamReader

/**
 * @author rleys
 *
 */
class DependenciesTests extends FeatureSpec with BeforeAndAfterAll {



  feature("Project Model Parsing") {

    scenario("Maven Project") {


      //-- Load File
      var xmlinputBuffer = new StAXIOBuffer(new InputStreamReader(getClass().getClassLoader.getResourceAsStream("model_maven_pom.xml")))

      //-- Parse
      var project = new Project
      project.appendBuffer(xmlinputBuffer)
      xmlinputBuffer.streamIn






    }



  }




}
