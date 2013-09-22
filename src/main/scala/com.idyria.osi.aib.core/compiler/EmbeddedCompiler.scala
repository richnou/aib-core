package com.idyria.osi.aib.core.compiler

import java.io._

import scala.io._
import scala.tools.nsc._
import scala.tools.nsc.reporters._
import scala.tools.nsc.interpreter._
 
import scala.runtime._
 
import java.net._

import scala.collection.JavaConversions._
 
class EmbeddedCompiler {


    // Run Statistics
    //---------------------

    /// Number of script runs
    var runCount = 0
    
    // Prepare Class Loader for compiler
    //-------------------------
    var bootclasspath = List[URL]()

    //--- Scala Compiler and library
    try {
	    val compilerPath = java.lang.Class.forName("scala.tools.nsc.Interpreter").getProtectionDomain.getCodeSource.getLocation
	    bootclasspath = compilerPath ::  bootclasspath 
	    val libPath = java.lang.Class.forName("scala.Some").getProtectionDomain.getCodeSource.getLocation
	    bootclasspath = libPath  :: bootclasspath 
    } catch {
      case e : Throwable => 
    }

    

    //-- If Classloader is an URL classLoader, add all its urls to the compiler
    //println("Classloader type: "+getClass().getClassLoader())
    if (getClass.getClassLoader.isInstanceOf[URLClassLoader]) {

        //println("Adding URLs from class loader to boot class path")

        //-- Gather URLS
        getClass.getClassLoader.asInstanceOf[URLClassLoader].getURLs().foreach( url => bootclasspath = url ::  bootclasspath )

    }


    // Prepare Compiler Settings Settings
    //----------------
    var settings2 = new GenericRunnerSettings({
        error => println(error)
    })
    settings2.usejavacp.value = true
    settings2.bootclasspath.value = bootclasspath  mkString java.io.File.pathSeparator

    settings2.outputDirs.setSingleOutput("target/classes")
    //-- Show some infos
    //println("compilerPath=" + compilerPath);
    //println("settings.bootclasspath.value=" + settings2.bootclasspath.value);

    // Create Compiler
    //---------------------
    val imain = new IMain(settings2)

    // Compilation result
    
    // Default compiler
    //---------------
    
  
    // Reporter
    var reporter = new ConsoleReporter(settings2)

    // Global
    val defaultCompiler = new Global(settings2,reporter)
        
       
    val defaultCompilerRun = new defaultCompiler.Run()

    /**
        Binds a named variable to a value for the model compiler
    */
    def bind(name:String,value: Any) = {

        imain.bindValue(name,value)
        //defaultCompilerRun.

    }

    def interpret(content:String) = {
      
      imain.interpret(content)
    
    }
  
    /**
        @return 
    */
    def compile(file: File) = {

        // Reporter
        var reporter = new ConsoleReporter(settings2)

        // Global
        val compiler = new Global(settings2,reporter)
        
        //println(s"CRun: "+global.currentRun)
        //global.compileFiles(List(file))

        val run = new compiler.Run()

        run.compile(List(file.getAbsolutePath))

        run.symSource.foreach {
            case (key,value) => println(s"Compiled symbol: "+key)
        }

/*
         // Get Script input
        //-----------------------
        var inputModel = Source.fromFile(file).mkString



        // Compile
        //--------------
        imain.compileString(inputModel) match {

            // OK -> Return Model
            case true =>
                
                

            case false =>
                throw new RuntimeException("Could not compile")
        }
*/

    }

}
