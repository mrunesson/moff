package org.linuxalert.moff

import java.net.URL
import java.net.URI
import scala.io.Source
import org.apache.commons.io.IOUtils
import java.nio.file._

class Downloader(val destination:LocalRepository) {

	def download(artifact:Artifact) : Seq[String] = {
		val repo = new RemoteRepository()
		val length = repo.getRepositoryURL().toString.length
		val relativeURLs = repo.getArtifactsComponentsURLs(artifact)

		def notExists = { (u:String) =>
			val localFile = Paths.get(destination.getRepositoryURL.toString + u.slice(length, u.length))
			!Files.exists(localFile.getParent)
		}

		def downloadURL = { (u:String) => 
			val remoteFile = new URL(u)
			val localFile = Paths.get(destination.getRepositoryURL.toString + u.slice(length, u.length))
			val source = remoteFile.openStream()
			Files.createDirectories(localFile.getParent)
			val dest = Files.newOutputStream(localFile)
			IOUtils.copyLarge(source, dest)
			source.close
			dest.close
			localFile.toString
		} 

		relativeURLs.filter(notExists).map(downloadURL)
	} 

}