package org.linuxalert.moff

import scala.xml._

/**
 * @author ${user.name}
 */
object Moff {



	def downloadArtifact(artifact:Artifact, downloader:Downloader) {
	    println("Downloading: " + artifact.getArtifactAsPath())
	    val repo = new RemoteRepository
		
		val newLocalFiles = downloader.download(artifact)
		val newPoms = newLocalFiles.filter(_.endsWith(".pom")).map(new Pom(_))
//		newPoms.map((pom:Pom) => pom.getDependencyArtifacts).flatten.foreach((a:Artifact) =>println("DepArtifacts found: " + a.getArtifactAsPath))
//		newPoms.map((pom:Pom) => pom.getParentArtifacts).flatten.foreach((a:Artifact) =>println("ParentArtifacts found: " + a.getArtifactAsPath))
//		newPoms.map((pom:Pom) => pom.getPluginArtifacts).flatten.foreach((a:Artifact) =>println("PluginArtifacts found: " + a.getArtifactAsPath))
//		val newArtifacts = newPoms.map((pom:Pom) => Seq.concat(pom.getParentArtifacts(),pom.getDependencyArtifacts(),pom.getPluginArtifacts())).flatten
		val newArtifacts = newPoms.map((pom:Pom) => Seq.concat(pom.getParentArtifacts(),pom.getDependencyArtifacts())).flatten
		val newArtifactsWithLatest = newArtifacts.map((a:Artifact)=>{
		  if (a.version.isEmpty()) repo.getLatestReleasedArtifact(a)
		  else if (a.version.equals("RELEASE")) repo.getLatestReleasedArtifact(a)
		  else if (a.version.equals("LATEST")) repo.getLatestArtifact(a)
		  else a
		})
//		newArtifactsWithLatest.foreach((a:Artifact)=>println("FOO: " + a.getArtifactAsPath))
		newArtifactsWithLatest.foreach(downloadArtifact(_, downloader))
		println
	}


	def main(args : Array[String]) { 
		val rootArtifact = new Artifact(args(0), args(1), args(2))
		val local = new LocalRepository("/tmp/foo")
		val downloader = new Downloader(local)
		val repo = new RemoteRepository
		rootArtifact.download(downloader, repo)
	}

}
