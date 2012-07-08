package org.linuxalert.moff
import java.net.URL
import scala.io.Source

trait Repository {
  
    def getURLForArtifact(artifact:Artifact): String
    
    def getArtifactsComponentsURLs(artifact:Artifact): Seq[String]
    
    def hasArtifact(artifact:Artifact): Boolean = {
      try {
    	  Source.fromURL(getURLForArtifact(artifact))
    	  return true
      } catch {
        case e:Exception => return false
      }
    }
 
    
    
}