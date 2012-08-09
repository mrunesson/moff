package org.linuxalert.moff

import java.net.URL
import java.net.URI
import scala.io.Source
import org.apache.commons.io.IOUtils
import java.nio.file._

/** Responsible for handling the download a specific artifact and store it on local storage.  
 * 
 *  Constructions requires a local repo destination for storage.
 *  
 *  TODO: This needs a refactoring to be more general.
 */
class Downloader(val destination:LocalRepository) {

    /** Download an Artifact and return a list of files downloaded. */
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