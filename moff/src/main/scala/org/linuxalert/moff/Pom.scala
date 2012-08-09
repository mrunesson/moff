package org.linuxalert.moff

import scala.xml._

/** Parsing and representing a pom.xml file. 
 * 
 *  The class is immutable.
 *  Construction takes a filename for a pom.xml-file. 
 */
class Pom(val pom:String) {

	private val rootNode = XML.load(pom);

	private def flattenPom(node:Node): Seq[Node] = {
			Seq.concat(node, node.child.map(flattenPom)).flatten
	} 

	private def getNodeGroupId(node:Node): String = {
			val groupIdNode = node.child.find(_.label.equalsIgnoreCase("groupId"));
			if (!groupIdNode.isEmpty) {
				groupIdNode.get.text
			} else ""
	}

	private def getNodeArtifactId(node:Node): String = {
			val artifactIdNode = node.child.find(_.label.equalsIgnoreCase("artifactId"));
			if (!artifactIdNode.isEmpty) {
				artifactIdNode.get.text
			} else "";
	}

	private def getNodeVersion(node:Node): String = {
			val versionNode = node.child.find(_.label.equalsIgnoreCase("version"));
			if (!versionNode.isEmpty) {
				versionNode.get.text
			} else "";
	}

	
	/**
	 * Returns properties from the POMs normal defined elements like artifactId, version and groupId.
	 */
	def getProjectProperties(): Map[String,String] = {
			Map(("${project.version}"->getNodeVersion(rootNode)),
					("${project.groupId}"->getNodeGroupId(rootNode)),
					("${project.ArtifactId}"->getNodeArtifactId(rootNode))).
			filter(!_._2.isEmpty())
	}

	
	/**
	 * Get properties from the POMs properties elements.
	 */
	def getProperties(node: Node=rootNode): Map[String,String] = {
			val findResult = node.child.find(_.label.equals("properties"));
			if (!findResult.isEmpty) {
				val propertiesNodes = findResult.get.child
						propertiesNodes.map((n:Node) => Map("${" + n.label + "}" ->n.text)).fold(Map())((i,s)=>i++s)
			} else {
				Map()
			}
	}
	
	def getAllProperties(): Map[String,String] = {
	    getProperties()++getProjectProperties()
	}

	
	private def getAllPluginNodes(): Seq[Node] = {
			flattenPom(rootNode).filter(_.label.equalsIgnoreCase("plugin"))
	}
	
	/**
	 * Get artifacts from all plugins referd by the POM.
	 */
	def getPluginArtifacts(): Seq[Artifact] = {
			getAllPluginNodes()
			.map((n:Node) => new Artifact(getNodeGroupId(n), getNodeArtifactId(n), getNodeVersion(n)))
			.map((a:Artifact) => {
				if (a.groupId.isEmpty()) new Artifact("org.apache.maven.plugins", a.artifactId, a.version)
				else a
			})
			.map(_.setProperties(getProperties()))
			.map(_.setProperties(getProjectProperties()))
	}

	
	private def getAllDependencyNodes(): Seq[Node] = {
	        rootNode.child.filter(_.label.equals("dependencies"))
	        .map(_.child).flatten
			.filter(_.label.equalsIgnoreCase("dependency"))
//			.filter(!_.child.exists(_.exists(_.text.equals("test"))))
			.filter(!_.child.exists(_.exists(_.text.equals("system"))))
			.filter(!_.child.exists(_.exists(_.text.equals("provided"))))
	}
	/**
	 * Get artifacts for all dependencies in the POM.
	 */
	def getDependencyArtifacts(): Seq[Artifact] = {
			getAllDependencyNodes()
			.map((n:Node) => new Artifact(getNodeGroupId(n), getNodeArtifactId(n), getNodeVersion(n)))
			.map(_.setProperties(getProperties()))
			.map(_.setProperties(getProjectProperties()))
	}

	
	private def getParentNodes(): Seq[Node] = {
			val parentNode=rootNode.child.find(_.label.equals("parent"));
			if (!parentNode.isEmpty) {
				List(parentNode.get)
			} else {
				List()
			}

	}
	
	/**
	 * Get an artifact of representing the parent of the POM
	 */
	def getParentArtifacts(): Seq[Artifact] = {
			getParentNodes()
			.map((n:Node) => new Artifact(getNodeGroupId(n), getNodeArtifactId(n), getNodeVersion(n)))
			.map(_.setProperties(getProperties()))
			.map(_.setProperties(getProjectProperties()))
	}


}