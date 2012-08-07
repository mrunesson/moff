package org.linuxalert.moff

import java.net.URL
import scala.io.Source
import scala.xml._

class RemoteRepository extends Repository {

	private val repositoryURL = new URL("http://central.maven.org/maven2/")

	def getRepositoryURL() : String = repositoryURL.toString

	def getURLForArtifact(artifact:Artifact): String =
	getRepositoryURL + artifact.getArtifactAsPath

	def getURLForArtifactMetaData(artifact:Artifact): String =
	getRepositoryURL + artifact.getArtifactWithoutVersionAsPath  + "maven-metadata.xml"

	def getArtifactsComponentsURLs(artifact:Artifact): Seq[String] = {
			val parserFactory = new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
					val parser = parserFactory.newSAXParser()
					val adapter = new scala.xml.parsing.NoBindingFactoryAdapter

					val artifactURL = getURLForArtifact(artifact)
					val source = new org.xml.sax.InputSource(artifactURL.toString())

			val getHrefOfA = { (n:Node) =>
			n.attribute("href").get.toString
			}

			val createAbsolutURL = { (s:String) =>
			artifactURL.toString + s
			}

			val rootNode = adapter.loadXML(source, parser)
					val bodyChildNodes = rootNode.child.filter(_.label == "body").map(_.child).flatten
					val preChildren = bodyChildNodes.filter(_.label == "pre").map(_.child).flatten
					val aNodes = preChildren.filter(_.label == "a")

					aNodes.map(getHrefOfA).filter(!_.equals("../")).map(createAbsolutURL)
	}

	/**
	 * Load maven-metadata.xml and return artifact of released version.
	 */
	def getLatestReleasedArtifact(artifact:Artifact): Artifact = {
//			println(getURLForArtifactMetaData(artifact))
			val rootNode=XML.load(getURLForArtifactMetaData(artifact))
			try {
				try {
					val releasedVersion = rootNode.child.find(_.label.equals("versioning")).get.child.find(_.label.equals("release")).get.text
							new Artifact(artifact.groupId, artifact.artifactId, releasedVersion)
				} catch {
				case _ => new Artifact(artifact.groupId, artifact.artifactId, rootNode.child.find(_.label.equals("version")).get.text)
				}
			} catch {
			case _ => new Artifact(artifact.groupId, artifact.artifactId, rootNode.child.find(_.label.equals("versioning")).get.child.find(_.label.equals("versions")).get.child.find(_.label.equals("version")).get.child.text)
			}
	}

	def getLatestArtifact(artifact:Artifact): Artifact = {
//			println(getURLForArtifactMetaData(artifact))
			val rootNode=XML.load(getURLForArtifactMetaData(artifact))
			try {
				val latestVersion = rootNode.child.find(_.label.equals("versioning")).get.child.find(_.label.equals("latest")).get.text
						new Artifact(artifact.groupId, artifact.artifactId, latestVersion)
			} catch {
			case _ => new Artifact(artifact.groupId, artifact.artifactId, rootNode.child.find(_.label.equals("version")).get.text)
			}
	}

}