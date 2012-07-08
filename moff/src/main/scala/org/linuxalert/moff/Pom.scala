package org.linuxalert.moff

import scala.xml._

class Pom(val pom:String) {

	private val rootNode = XML.load(pom);
	private val properties = getProperties(rootNode);
	private val parentArtifact = {
			val parentNode=rootNode.child.find(_.label.equals("parent"));
			if (!parentNode.isEmpty) {
				createArtifact(parentNode.get, List(), false);
			} else {
				new Artifact("","","")
			}
	}


	private val pomArtifact = createArtifact(rootNode, List(parentArtifact), false);


	private def flattenPom(node:Node): Seq[Node] = {
			Seq.concat(node, node.child.map(flattenPom)).flatten
	} 


	private def createArtifact = { (node:Node, inherited:List[Artifact], plugin:Boolean) =>
//	println(pom)
	val groupIdNode = node.child.find(_.label.equalsIgnoreCase("groupId"))
	var groupId : String = "";
	if (!groupIdNode.isEmpty) {
		groupId = groupIdNode.get.text
	} else {
		if (plugin) groupId="org.apache.maven.plugins"
				else {
					println("For " + pom + " guessing groupId.")
					groupId = inherited.filterNot(_.groupId.isEmpty)(0).groupId
				}
	}

	val artifactIdNode = node.child.find(_.label.equalsIgnoreCase("artifactId"))
			var artifactId : String = "";
	if (!artifactIdNode.isEmpty) {
		artifactId = artifactIdNode.get.text
	} else {
		println("For " + pom + " guessing artifactId.")
		artifactId = inherited.filterNot(_.artifactId.isEmpty)(0).artifactId
	}

	val versionNode = node.child.find(_.label.equalsIgnoreCase("version"))
			var version : String = "";
	if (!versionNode.isEmpty) {
		version = versionNode.get.text
	} else {
		println("For " + pom + " using latest released version for " + artifactId)
		version = ""
	} 

	println("New artifact created: " + groupId + " " + artifactId + " " + version + ".")
	new Artifact(groupId, artifactId, version)

	}

	def getProjectProperties(): Map[String,String] = {
//			println("ProjectProperties: " + parentArtifact.version + " " + parentArtifact.groupId)
			Map(("${project.version}"->parentArtifact.version),
					("${project.groupId}"->parentArtifact.groupId))
	}

	def getProperties(node: Node=rootNode): Map[String,String] = {
			val findResult = node.child.find(_.label.equals("properties"));
			if (!findResult.isEmpty) {
				val propertiesNodes = findResult.get.child
						propertiesNodes.map((n:Node) => Map("${" + n.label + "}" ->n.text)).fold(Map())((i,s)=>i++s)
			} else {
				Map()
			}
	}

	private def getAllPluginNodes(): Seq[Node] = {
			flattenPom(rootNode).filter(_.label.equalsIgnoreCase("plugin"))
	}

	def getPluginArtifacts(): Seq[Artifact] = {
			getAllPluginNodes().map(createArtifact(_,List(pomArtifact, parentArtifact), true))
			.map(_.setProperties(getProperties()))
			.map(_.setProperties(getProjectProperties()))
	}

	private def getAllDependencyNodes(): Seq[Node] = {
			flattenPom(rootNode)
				.filter(_.label.equalsIgnoreCase("dependency"))
				.filter(!_.child.exists(_.exists(_.text.equals("test"))))
	}

	def getDependencyArtifacts(): Seq[Artifact] = {
			getAllDependencyNodes().map(createArtifact(_,List(pomArtifact, parentArtifact), false))
			.map(_.setProperties(getProperties()))
			.map(_.setProperties(getProjectProperties()))
	}

	private def getParentNodes(): Seq[Node] = {
			val parentNode=rootNode.child.find(_.label.equals("parent"))
					if (!parentNode.isEmpty) {
						List(parentNode.get)
					} else {
						List()
					}

	}

	def getParentArtifacts(): Seq[Artifact] = {
			getParentNodes().map(createArtifact(_,List(), false))
			.map(_.setProperties(getProperties()))
			.map(_.setProperties(getProjectProperties()))
	}


}