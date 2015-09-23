package com.idyria.osi.aib.core.compiler

import java.io._
import java.net._
import java.util.concurrent.Semaphore
import scala.collection.JavaConversions._
import scala.tools.nsc._
import scala.tools.nsc.interpreter._
import scala.reflect.internal.util.SourceFile
import scala.reflect.internal.util.BatchSourceFile
import scala.reflect.io.AbstractFile
import com.idyria.osi.tea.os.OSDetector

class EmbeddedCompiler (  var parentLoader : ClassLoader = null) {

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
    bootclasspath = compilerPath :: bootclasspath
    val libPath = java.lang.Class.forName("scala.Some").getProtectionDomain.getCodeSource.getLocation
    bootclasspath = libPath :: bootclasspath
  } catch {
    case e: Throwable =>
  }

  //-- If Classloader is an URL classLoader, add all its urls to the compiler
  //println("Classloader type: "+getClass().getClassLoader())
  //,Thread.currentThread().getContextClassLoader
  List(getClass.getClassLoader,Thread.currentThread().getContextClassLoader).foreach {
    case cl : URLClassLoader => 
      //-- Gather URLS
      cl.getURLs().foreach {
        url => 
         // println(s"**ECompiler adding: ${url.toExternalForm()}")
          bootclasspath = url :: bootclasspath
        }
    case _ => 
  }


  // Prepare Compiler Settings Settings
  //----------------
  var settings2 = new GenericRunnerSettings({
    error => println("******* Error Happened ***********")
  })
  settings2.nc.value = true
  settings2.usejavacp.value = true
  
  if (OSDetector.getOS==OSDetector.OS.LINUX) {
    
    settings2.classpath.value = bootclasspath mkString java.io.File.pathSeparator
    settings2.bootclasspath.value = bootclasspath mkString java.io.File.pathSeparator
  
    
    
  } else {
    
    settings2.classpath.value = bootclasspath.map(u => u.getPath.replaceFirst("/", "")) mkString java.io.File.pathSeparator
    settings2.bootclasspath.value = bootclasspath.map(u => u.getPath.replaceFirst("/", "")) mkString java.io.File.pathSeparator
  
    
  }
  
  if (new File("target/classes").exists()) {
    settings2.outputDirs.setSingleOutput("target/classes")
  } else {
    
  }
  
  //-- Show some infos
  //println("compilerPath=" + compilerPath);
  //println("settings.bootclasspath.value=" + settings2.bootclasspath.value);

  // Create Compiler
  //---------------------
  

  
  var interpreterOutput = new StringWriter
  val imain = new IMain(settings2, new PrintWriter(interpreterOutput)) {
    
    override protected def parentClassLoader: ClassLoader = {
      
      /*parentLoader match {
        case null => super.parentClassLoader
        case _ => parentLoader
      }*/
      Thread.currentThread().getContextClassLoader
   
    }
    
    def compileSourcesSeq(sources: Seq[SourceFile]): Boolean =
      compileSourcesKeepingRun(sources: _*)._1
  }
  
  /*{
    

    override private def makeClassLoader(): util.AbstractFileClassLoader =
      
      super.makeClassLoader()
   
    
  }*/

  // Compilation result

  // Default compiler
  //---------------

  // Reporter
 // var reporter = new ConsoleReporter(settings2)

  // Global
 // val defaultCompiler = new Global(settings2, reporter)

 // val defaultCompilerRun = new defaultCompiler.Run()

  
  // Ready Logic
  //--------------------
  var readySemaphore = new Semaphore(0)
  
  /**
   * Just execute a dummy line on compiler to init it, and release a grant in ready semaphore
   */
  def init = {
    
    interpret("var init = true;")
    readySemaphore.release()
    
  }
  
  /**
   * Wait for ready signal
   * init must have been called by user somewhere for this method to return, otherwise it keeps blocking
   */
  def waitReady = {
    
    readySemaphore.acquire
    readySemaphore.release
  }
  
  
  /**
   * Binds a named variable to a value for the model compiler
   */
  def bind(name: String, value: Any) = {

    imain.bind(name, value)
    //defaultCompilerRun.

  }

  /**
   * Will throw an exception with message in case of error
   */
  def interpret(content: String) = {

    try {
      // Interpret
      imain.interpret(content) match {
        case IR.Error => throw new RuntimeException(s"Could not interpret content: ${interpreterOutput.toString()}")
        case _        =>
      }

    } finally {

      // Reset output
      interpreterOutput.getBuffer().setLength(0)

    }

  }

  /**
   * Just compile a string, don't run it
   */
  def compile(content: String) = {

    try {

      imain.compileString(content) match {
        case false => throw new RuntimeException(s"Could not compile content: ${interpreterOutput.toString()}")
        case _     =>
      }

    } finally {

      // Reset output
      interpreterOutput.getBuffer().setLength(0)

    }

  }
  
  /**
   * Compile Some files
   */
  def compileFiles(f:Seq[File]) : Option[FileCompileError] = {
    
    try {

      imain.compileSourcesSeq(f.map{f => new BatchSourceFile(AbstractFile.getFile(f.getAbsoluteFile))}) match {
        case false => 
          
          // Prepare error
          Some(new FileCompileError(null,interpreterOutput.toString().trim))
          
          //throw new RuntimeException(s"Could not compile content: ${interpreterOutput.toString()}")
        case _     => None
      }

    } finally {

      // Reset output
      interpreterOutput.getBuffer().setLength(0)

    }
    
  }
  
  /**
   * Compile a file
   */
  def compileFile(f:File) : Option[FileCompileError] = {
    
    try {

      imain.compileSources(new BatchSourceFile(AbstractFile.getFile(f.getAbsoluteFile))) match {
        case false => 
          
          // Prepare error
          Some(new FileCompileError(f,interpreterOutput.toString()))
          
          //throw new RuntimeException(s"Could not compile content: ${interpreterOutput.toString()}")
        case _     => None
      }

    } finally {

      // Reset output
      interpreterOutput.getBuffer().setLength(0)

    }
    
  }

  /**
   * @return
   */
  /*def compile(file: File) = {

    // Reporter
    var reporter = new ConsoleReporter(settings2)

    // Global
    val compiler = new Global(settings2, reporter)

    //println(s"CRun: "+global.currentRun)
    //global.compileFiles(List(file))

    val run = new compiler.Run()

    run.compile(List(file.getAbsolutePath))

    run.symSource.foreach {
      case (key, value) => println(s"Compiled symbol: " + key)
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

  }*/

}

class CompileError {
  
}

class FileCompileError(var file : File,var message:String) extends CompileError {
  
}

object CompileError {
  
}
