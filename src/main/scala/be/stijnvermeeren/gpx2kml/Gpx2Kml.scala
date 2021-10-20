package be.stijnvermeeren.gpx2kml

import java.io.File
import scala.xml.XML

object Gpx2Kml {
  def convert(
    inDirectory: String,
    outPath: String,
    title: String,
    lineColor: String,
    lineWidth: Int
  ): Unit = {
    val d = new File(inDirectory)
    if (d.exists && d.isDirectory) {
      val tracks = d.listFiles.filter(_.isFile).filter(_.getName.endsWith(".gpx")).sortBy(_.getName) flatMap { file =>
        for (track <- XML.loadFile(file) \\ "trkseg") yield {
          val points = for (point <- track \\ "trkpt") yield {
            Coord(
              point.attribute("lon").get.toString.toDouble,
              point.attribute("lat").get.toString.toDouble
            )
          }

          <Placemark>
            <name>{file.getName}</name>
            <description></description>
            <styleUrl>#lineStyle</styleUrl>
            <LineString>
              <altitudeMode>clampToGround</altitudeMode>
              <extrude>1</extrude>
              <tessellate>1</tessellate>
              <coordinates>
                {Distance.removeRedundant(points) mkString " "}
              </coordinates>
            </LineString>
          </Placemark>
        }
      }

      val result =
        <kml xmlns="http://www.opengis.net/kml/2.2">
          <Document>
            <name>{title}</name>
            <Style id="lineStyle">
              <LineStyle>
                <color>{lineColor}</color>
                <width>{lineWidth}</width>
              </LineStyle>
            </Style>{tracks}
          </Document>
        </kml>

      scala.xml.XML.save(outPath, result, enc = "UTF-8", xmlDecl = true)
    } else {
      throw new Exception(s"Invalid directory $inDirectory")
    }
  }
}
