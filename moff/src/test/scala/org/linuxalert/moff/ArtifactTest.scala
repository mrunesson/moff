package org.linuxalert.moff

import org.scalatest.Assertions
import org.junit.Test

class ArtifactTest {

    @Test def getRepositoryURLShouldReturnMavenCentralURL() {
    val toTest = new Artifact("foo.bar","gazonk.muu","3.0")
    assert(toTest.getArtifactAsPath()=="foo" + java.io.File.separator + "bar" +
            java.io.File.separator + "gazonk.muu" + java.io.File.separator + "3.0" + java.io.File.separator)
    assert(toTest.getArtifactWithoutVersionAsPath()=="foo" + java.io.File.separator + "bar" +
            java.io.File.separator + "gazonk.muu" + java.io.File.separator)
  }
      
}