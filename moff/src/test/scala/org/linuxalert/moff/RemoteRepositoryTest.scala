package org.linuxalert.moff

import org.scalatest.Assertions
import org.junit.Test

class RemoteRepositoryTest {
  
  @Test def getArtifactsComponentsURLsShouldReturnListOffMavenCentralURLs() {
    val artifact = new Artifact("org.apache.maven.plugins","maven-deploy-plugin","2.7")
    val repo = new RemoteRepository;
    val artifactURL = repo.getURLForArtifact(artifact) 
    val result = repo.getArtifactsComponentsURLs(artifact)
    assert(artifactURL.toString().equalsIgnoreCase("http://central.maven.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/2.7/"))
    assert(result.length == 30)
  }
  

}