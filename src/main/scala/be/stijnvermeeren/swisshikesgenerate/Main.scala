package be.stijnvermeeren.swisshikesgenerate

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.util.StringUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git

import java.io.{ByteArrayInputStream, File, InputStream}
import java.util.{Locale, UUID}
import scala.xml.PrettyPrinter

object Main extends App {
  val config = ConfigFactory.load().getConfig("be.stijnvermeeren.swisshikes-generate")

  // Enforce correct formatting for floating point numbers (other locales might e.g. use a comma instead of a period)
  Locale.setDefault(Locale.US)

  val title = config.getString("title")
  val lineColor = config.getString("lineColor")
  val lineWidth = config.getInt("lineWidth")
  val maxPointsPerLine = config.getInt("maxPointsPerLine")
  val dataRepo = config.getString("dataRepo")

  val awsBucket = config.getString("aws.bucket")
  val awsRegion = config.getString("aws.region")

  val tmpDir = new File(s"/tmp/${UUID.randomUUID()}")
  println(s"Using temporary directory ${tmpDir.getAbsolutePath}")

  Git.cloneRepository
    .setDepth(1)
    .setURI(dataRepo)
    .setDirectory(tmpDir)
    .call()
    .close()

  Runtime.getRuntime.addShutdownHook(
    new Thread(() => FileUtils.deleteDirectory(tmpDir))
  )

  val s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(awsRegion)).build()
  val printer = new PrettyPrinter(80, 2)

  val fileData = tmpDir.listFiles.filter(_.isDirectory).filterNot(_.isHidden) map { yearDir =>
    val XmlData(kml, latestDate) = Generate.xmlFromDir(yearDir, title, lineColor, lineWidth, maxPointsPerLine)

    val fileName = s"${yearDir.getName}.kml"
    s3.putObject(
      awsBucket,
      fileName,
      printer.format(kml)
    )

    FileData(fileName = fileName, latestDate = latestDate)
  }

  val metadata = Metadata(
    latestDate = fileData.flatMap(_.latestDate).maxOption,
    files = fileData.map(_.fileName).toList
  )

  val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)
  val contentBytes = objectMapper.writeValueAsString(metadata).getBytes(StringUtils.UTF8)

  val objectMetadata = new ObjectMetadata()
  objectMetadata.setContentType("application/json")

  s3.putObject(
    awsBucket,
    "metadata.json",
    new ByteArrayInputStream(contentBytes): InputStream,
    objectMetadata
  )
}
