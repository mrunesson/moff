package org.linuxalert.moff

class Artifact(val groupId:String, val artifactId:String, val versionOrg:String) {

    val version = versionOrg.stripSuffix("-SNAPSHOT")
  
	def getArtifactAsPath():String = {
			getArtifactWithoutVersionAsPath + version + "/"
	}

	def getArtifactWithoutVersionAsPath():String = {
			groupId.replace(".","/") + "/" + artifactId + "/"
	}
	
	def setProperties(properties:Map[String,String]):Artifact = {
//	    println("Source artifact: " + groupId + " " + artifactId + " " + version + ".")
//	    println("New artifact created from prop: " + properties.getOrElse(groupId,groupId) + " " + properties.getOrElse(artifactId,artifactId) + " " + properties.getOrElse(version,version) + ".")
		new Artifact(properties.getOrElse(groupId,groupId), 
		    properties.getOrElse(artifactId,artifactId),
		    properties.getOrElse(version,version))
	}
	
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
	
	def download(downloader:Downloader, repo: RemoteRepository) {
	    downloadInternal(0, downloader, repo)
	}
	
	private def printSpace(n:Integer) {
	    for (i <- 0 until n) {
            print("    ")
	    }
	}
	
	private def downloadInternal(level:Integer, downloader:Downloader, repo: RemoteRepository, inheritedProperties: Map[String,String]=Map()):Map[String,String] = {
		printSpace(level)
	    println("Downloading: " + getArtifactAsPath())
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
		
		// Download plugins and its dependencies. ????
		printSpace(level)
		println("Ready downloading: " + getArtifactAsPath())
		properties
	}

}