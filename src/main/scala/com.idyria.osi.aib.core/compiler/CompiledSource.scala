package com.idyria.osi.aib.core.compiler

import java.net.URL
import java.io.File

/**
 * Represents a compiled source metadata from an URL
 * 
 * It contains methods to help application determine if the source has been changed
 * 
 */
class CompiledSource[T]( var source : URL,var result : T) {
  
  
	var lastModified : Long = 0
	
	if (source.getProtocol()=="file") {
	  this.lastModified = new File(source.getFile()).lastModified()
	}
	
  
	def isValid : Boolean = {
	  
	  source.getProtocol() match {
	    case "file" => 
	    	
	      var actualLastModified = new File(source.getFile()).lastModified()
	      
	      //println(s"Testing source file: "+source.getFile()+ s", last modified: $actualLastModified, last compiled: $lastModified")
	      
	      new File(source.getFile()).lastModified() match {
	        case lm if (lm > lastModified) =>
	          
	          lastModified = lm
	          false
	        case _ => true
	      }
	      
	    case p => 
	      println("Unknown protocol: "+p)
	      true
	  }
	  
	}
  
}


/**
 * A Trait for an Object that can be used to compile an URL source to a specific Type
 * 
 */
trait SourceCompiler[T] {
  
  // Compilation cache
  //--------------------------
  var cache = Map[String,CompiledSource[T]]()
  
  // Create Compiler
  //-------------
  var compiler = new EmbeddedCompiler


  // Configured Imports
  //---------------
 /* var compileImports = List[Class[_]]()

  def addCompileImport(cl: Class[_]) = {
    compileImports.contains(cl) match {
      case false ⇒ compileImports = compileImports :+ cl
      case _     ⇒
    }
  }*/
  
  /**
   * User access to compilation
   */
  def compile(source: URL) = {
   
    
    // Look in map
    //---------------------
    this.cache.get(source.toExternalForm()) match {
      
      //-- Still valid, return result
      case Some(compiled) if (compiled.isValid) => 
        
        //println(s"Already compil")
        compiled.result
        
      //-- None or invalid, recompile and save
      case _ => 
        
        //-- Compile
        var res = this.doCompile(source)
        
        //-- Save of Update
        this.cache.get(source.toExternalForm()) match {
          case Some(compiled) => compiled.result = res
          case None => this.cache = this.cache + (source.toExternalForm() -> new CompiledSource(source,res))
        }
        
        
        //-- Return
        res
        
    }
    
  }
  
  /**
   * Implementation when needing to compile
   */
  protected def doCompile(source: URL) : T
  
  
  
}