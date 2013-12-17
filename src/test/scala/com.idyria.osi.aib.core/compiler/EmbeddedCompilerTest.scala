package com.idyria.osi.aib.core.compiler

import java.io._
import com.idyria.osi.ooxoo.core.buffers.datatypes._
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax._
import org.scalatest._
import com.idyria.osi.ooxoo.model.writers._
import com.idyria.osi.ooxoo.model.out.scala._
import scala.language.postfixOps
import com.idyria.osi.tea.timing.TimingSupport

class EmbeddedCompilerTest extends FunSuite with GivenWhenThen with TimingSupport {

  test("Init Time") {

    var res = time {

      var compiler = new EmbeddedCompiler
      compiler.init
      compiler.waitReady

     
      
    }
    
     println(s"Inited Compiler in $res ms")

  }

  test("Find compiled Types") {

    var compiler = new EmbeddedCompiler
    compiler.init

    compiler.interpret("""var res = "Hello"""")

    var found = compiler.imain.valueOfTerm("res")

    println(s"Found term: " + found)
    //compiler.compile(new File("src/test/resources/test_compile.scala"))

  }

}
