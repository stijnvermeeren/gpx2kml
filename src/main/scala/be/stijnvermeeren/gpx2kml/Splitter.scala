package be.stijnvermeeren.gpx2kml

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.xml.XML


case class DataPoint(name: String, description: String, coordinates: String) {
  val trimmedName: String = {
    val digitOrHyphen = name.takeWhile(char => char.isDigit || char == '-')
    digitOrHyphen.reverse.dropWhile(_ == '-').reverse
  }
}

object Splitter extends App {
  val inDir = new File("/Users/stijn/Workspace/stijnvermeeren-frontend/public/files/swisshikes")
  val outDir = Paths.get("/Users/stijn/Workspace/swisshikes-data")
  if (inDir.exists && inDir.isDirectory) {
    inDir.listFiles.filter(_.isFile).filter(_.getName.endsWith(".kml")).sortBy(_.getName) foreach { file =>
      val year = file.getName.split('.').head
      for (placemark <- XML.loadFile(file) \\ "Placemark") {
        val dataPoints = for {
          name <- placemark \\ "name"
          description <- placemark \\ "description"
          coordinates <- placemark \\ "coordinates"
        } yield DataPoint(name.text, description.text, coordinates.text.trim)

        for {
          group <- dataPoints.groupBy(_.name).values
          (dataPoint @ DataPoint(name, description, coordinates), index) <- group.zipWithIndex
        } {
          val outName = if (group.size > 1) s"${dataPoint.trimmedName}-${index + 1}" else dataPoint.trimmedName
          val metadata = Seq(
            s"date: $name",
            s"description: $description"
          ).mkString("\n")
          write(outDir.resolve(year).resolve(s"$outName.coordinates.txt"), coordinates)
          write(outDir.resolve(year).resolve(s"$outName.metadata.yml"), metadata)
        }
      }
    }
  }

  def write(target: Path, contents: String): Unit = {
    Files.createDirectories(target.getParent)
    Files.write(target, s"$contents\n".getBytes(StandardCharsets.UTF_8))
  }
}
