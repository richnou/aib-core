package com.idyria.osi.aib.core.compiler

import java.io._

import com.idyria.osi.ooxoo.core.buffers.datatypes._
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax._
import org.scalatest._

import com.idyria.osi.ooxoo.model.writers._
import com.idyria.osi.ooxoo.model.out.scala._

import scala.language.postfixOps



class EmbeddedCompilerTest extends FunSuite with GivenWhenThen {

    test("Find compiled Types")  {

        var compiler = new EmbeddedCompiler

        compiler.compile(new File("src/test/resources/test_compile.scala"))

    }

}
