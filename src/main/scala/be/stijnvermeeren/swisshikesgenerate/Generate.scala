package be.stijnvermeeren.swisshikesgenerate

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.File
import java.nio.file.Files
import scala.xml.{Elem, PrettyPrinter, XML}

object Generate {
  final case class MetaData(date: Option[String], description: Option[String], albums: Option[List[String]])

  def findCoordinatesFile(files: Seq[File]): Option[String] = {
    files.find(_.getName.endsWith(".coordinates.txt")).map { coordinatesFile =>
      Files.readAllLines(coordinatesFile.toPath).toArray.mkString(" ")
    }
  }

  def findGpxFile(files: Seq[File], maxPointsPerLine: Int): Option[String] = {
    files.find(_.getName.endsWith(".gpx")).flatMap { file =>
      val maxPointsEnforcer = new MaxPointsEnforcer(maxPointsPerLine)

      for (track <- (XML.loadFile(file) \\ "trkseg").headOption) yield {
        val points = for (point <- track \\ "trkpt") yield {
          Coord(
            point.attribute("lon").get.toString.toDouble,
            point.attribute("lat").get.toString.toDouble
          )
        }

        maxPointsEnforcer.reducePoints(points) mkString " "
      }
    }
  }

  def xmlFromDir(yearDir: File, title: String, lineColor: String, lineWidth: Int, maxPointsPerLine: Int): XmlData = {
    val year = yearDir.getName
    val data = for {
      (name, files) <- yearDir.listFiles.groupBy(_.getName.split("[\\._]]").head).toList.sortBy(_._1)
      track <- findCoordinatesFile(files) orElse findGpxFile(files, maxPointsPerLine)
    } yield {
      val metaData = files.find(_.getName.endsWith(".metadata.yml")).map { metaDataFile =>
        val mapper = new ObjectMapper(new YAMLFactory())
        mapper.registerModule(DefaultScalaModule)
        mapper.readValue(metaDataFile, classOf[MetaData])
      }
      val title = metaData.flatMap(_.date).getOrElse(name)
      val description = metaData.flatMap(_.description)

      val albums = metaData.flatMap(_.albums).getOrElse(List.empty)
      val albumsDescription = if (albums.nonEmpty) {
        val links = albums.map(link => s"""<a href="$link" target="_blank">$link</a>""").mkString(", ")
        Some(s"Photos: $links")
      } else {
        None
      }

      val fullDescription = (description.toSeq ++ albumsDescription).mkString("<br /><br />")

      val placemark = <Placemark>
        <name>{title}</name>
        {if (fullDescription.nonEmpty) <description>{fullDescription}</description> else {}}
        <styleUrl>#lineStyle</styleUrl>
        <LineString>
          <altitudeMode>clampToGround</altitudeMode>
          <extrude>1</extrude>
          <tessellate>1</tessellate>
          <coordinates>{track}</coordinates>
        </LineString>
      </Placemark>

      XmlData(placemark, metaData.flatMap(_.date))
    }

    val kml = <kml xmlns="http://www.opengis.net/kml/2.2">
      <Document>
        <name>{title} - {year}</name>
        <Style id="lineStyle">
          <LineStyle>
            <color>{lineColor}</color>
            <width>{lineWidth}</width>
          </LineStyle>
        </Style>
        {data.map(_.xml)}
      </Document>
    </kml>

    XmlData(kml, data.map(_.latestDate).max)
  }
}
