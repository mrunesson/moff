package org.linuxalert.moff

import java.net.URL
import scala.io.Source

class LocalRepository(val path:String) extends Repository {

  private val repositoryURL = path + "/"
  
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