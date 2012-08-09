package org.linuxalert.moff

import scala.xml._

/**
 * Main class for Moff; Maven Offline.
 * 
 * @author Magnus Runesson
 */
object Moff {



	def downloadArtifact(artifact:Artifact, downloader:Downloader) {
	    println("Downloading: " + artifact.getArtifactAsPath())
	    val repo = new RemoteRepository
		
		val newLocalFiles = downloader.download(artifact)
		val newPoms = newLocalFiles.filter(_.endsWith(".pom")).map(new Pom(_))
		val newArtifacts = newPoms.map((pom:Pom) => Seq.concat(pom.getParentArtifacts(),pom.getDependencyArtifacts())).flatten
		val newArtifactsWithLatest = newArtifacts.map((a:Artifact)=>{
		  if (a.version.isEmpty()) repo.getLatestReleasedArtifact(a)
		  else if (a.version.equals("RELEASE")) repo.getLatestReleasedArtifact(a)
		  else if (a.version.equals("LATEST")) repo.getLatestArtifact(a)
		  else a
		})
		newArtifactsWithLatest.foreach(downloadArtifact(_, downloader))
		println
	}


	def main(args : Array[String]) {
	    if (args.length!=3) {
	        System.err.println("Wrong number of arguments.")
	        System.err.println("Following arguments, in given order, must be: groupId artifactId version")
	        System.exit(1)
	    }
		val rootArtifact = new Artifact(args(0), args(1), args(2))
		val local = new LocalRepository("moff")
		val downloader = new Downloader(local)
		val repo = new RemoteRepository
		rootArtifact.download(downloader, repo)
	}

}
