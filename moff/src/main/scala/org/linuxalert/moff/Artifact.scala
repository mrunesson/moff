package org.linuxalert.moff

class Artifact(val groupId:String, val artifactId:String, val versionOrg:String) {

    val version = versionOrg.stripSuffix("-SNAPSHOT")
  
	def getArtifactAsPath():String = {
			groupId.replace(".","/") + "/" + artifactId.replace(".","/") + "/" + version + "/"
	}

	def getArtifactWithoutVersionAsPath():String = {
			groupId.replace(".","/") + "/" + artifactId.replace(".","/") + "/"
	}
	
	def setProperties(properties:Map[String,String]):Artifact = {
//	    println("Source artifact: " + groupId + " " + artifactId + " " + version + ".")
//	    println("New artifact created from prop: " + properties.getOrElse(groupId,groupId) + " " + properties.getOrElse(artifactId,artifactId) + " " + properties.getOrElse(version,version) + ".")
		new Artifact(properties.getOrElse(groupId,groupId), 
		    properties.getOrElse(artifactId,artifactId),
		    properties.getOrElse(version,version))
	}
	

}