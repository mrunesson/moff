package org.linuxalert.moff

import java.lang.Integer;

/** Immutable class representing a Maven artifact.
 * 
 * Constructor requires groupId, artifactId and version. Version can be a
 * version number, an empty string, RELEASE or LATEST.
 */
class Artifact(val groupId:String, val artifactId:String, val versionOrg:String) {

    val version = versionOrg.stripSuffix("-SNAPSHOT")
  
    /** Get full path for artifact. */
	def getArtifactAsPath():String = {
			getArtifactWithoutVersionAsPath + version + java.io.File.separator
	}

    /** Get path for artifact without version number. */
	def getArtifactWithoutVersionAsPath():String = {
			groupId.replace(".",java.io.File.separator) + java.io.File.separator + artifactId + java.io.File.separator
	}
	
	/** Return a new artifact with provided properties set. */
	def setProperties(properties:Map[String,String]):Artifact = {
		new Artifact(properties.getOrElse(groupId,groupId), 
		    properties.getOrElse(artifactId,artifactId),
		    properties.getOrElse(version,version))
	}
	
	/** Resolve what version to download of the artifact using artifact 
	 *  meta data and information from repo. Returns a new Artifact with
	 *  real version number set.
	 */
	def resolveArtifactVesion(repo: RemoteRepository):Artifact = {
	    try {
		  if (this.version.isEmpty()) repo.getLatestReleasedArtifact(this)
		  else if (this.version.equals("RELEASE")) repo.getLatestReleasedArtifact(this)
		  else if (this.version.equals("LATEST")) repo.getLatestArtifact(this)
		  else this
	    } catch {
	        case _ => {
	            println("*** Failed: " + getArtifactAsPath())
	            new Artifact("","","")
	        }
	    }
	}
	
	/**
	 * Downloading this artifact using provided downloader from repository repo.
	 */
	def download(downloader:Downloader, repo: RemoteRepository) {
	    downloadInternal(0, downloader, repo)
	}
	
	/** Helper method to indent printing. */
	private def printIndent(n:Integer, s:String) {
	    for (i <- 0 until n) {
            print(" ")
	    }
	    println(s)
	}
	
	/** Helper function for download function doing the actual work.
	 *  Resolving properties and dependencies, then downloading dependencies.
	 */
	private def downloadInternal(level:Integer, downloader:Downloader, repo: RemoteRepository, inheritedProperties: Map[String,String]=Map()):Map[String,String] = {
		printIndent(level, "Downloading: " + getArtifactAsPath())
		var newLocalFiles: Seq[String] = null;
		var newPoms: Seq[Pom] = null;
	    try {
	    	newLocalFiles = downloader.download(this)    
		    newPoms = newLocalFiles.filter(_.endsWith(".pom")).map(new Pom(_))
		} catch {
	        case _ => {
	        println("*** Failed: " + getArtifactAsPath())
	        return Map[String,String]()
	        }
	    }
		val pomProperties = newPoms.foldLeft(Map[String,String]())((x,y)=>x++y.getAllProperties)
		
		// Start download parent and all its dependencies.
		val newParentArtifacts = newPoms.map((pom:Pom) => pom.getParentArtifacts())
			.flatten
			.map(_.resolveArtifactVesion(repo))
		val parentProperties = newParentArtifacts
			.map(_.downloadInternal(level+1, downloader,repo))
			.foldLeft(Map[String,String]())((x,y)=>x++y)
		
		//val properties=inheritedProperties++parentProperties++PomsProperties
		val properties=inheritedProperties++parentProperties++pomProperties
		
		// Download dependency artifacts and its dependencies.
		val newArtifacts = newPoms.map((pom:Pom) => pom.getDependencyArtifacts())
			.flatten
			.map(_.resolveArtifactVesion(repo))
			.map(_.setProperties(properties))
		newArtifacts.foreach(_.downloadInternal(level+1, downloader,repo, properties))

		
		// Download plugin artifacts and its dependencies.
		val newPluginArtifacts = newPoms.map((pom:Pom) => pom.getPluginArtifacts())
			.flatten
			.map(_.resolveArtifactVesion(repo))
			.map(_.setProperties(properties))
			.filter(!_.groupId.isEmpty)
		newPluginArtifacts.foreach(_.downloadInternal(level+1, downloader,repo, properties))
		
		printIndent(level, "Ready: " + getArtifactAsPath())
		
		properties
	}

}