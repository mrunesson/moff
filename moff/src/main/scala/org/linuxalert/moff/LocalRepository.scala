package org.linuxalert.moff

import java.net.URL
import scala.io.Source

/** Represents a local repository.
 * 
 * It does not share common interface due to lack of support of file:// urls in JVM.
 * This may be fixed in future using Apache commons.
 */
class LocalRepository(val path:String) extends Repository {

  private val repositoryURL = path + java.io.File.separator
  
  def getRepositoryURL() : String = repositoryURL
  
  def getURLForArtifact(artifact:Artifact): String =
    getRepositoryURL + artifact.getArtifactAsPath
    
  def getArtifactsComponentsURLs(artifact:Artifact): Seq[String] = {
    val artifactURL = getURLForArtifact(artifact)
    if (hasArtifact(artifact))
      Source.fromURL(artifactURL).getLines().toList.map({ (e:String) => artifactURL.toString() + e })
    else
      Nil
  }
  
}