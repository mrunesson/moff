package org.linuxalert.moff

import org.scalatest.Assertions
import org.junit.Test

class ArtifactTest {

    @Test def getRepositoryURLShouldReturnMavenCentralURL() {
    val toTest = new Artifact("foo.bar","gazonk.muu","3.0")
    assert(toTest.getArtifactAsPath()=="foo/bar/gazonk.muu/3.0/")
    assert(toTest.getArtifactWithoutVersionAsPath()=="foo/bar/gazonk.muu/")
  }
      
}