package be.stijnvermeeren.gpx2kml

import com.typesafe.config.ConfigFactory
import org.eclipse.jgit.api.Git

import java.io.File
import java.nio.file.Files
import java.util.Locale

object Main extends App {
  val config = ConfigFactory.load().getConfig("be.stijnvermeeren.swisshikes-generate")

  // Enforce correct formatting for floating point numbers (other locales might e.g. use a comma instead of a period)
  Locale.setDefault(Locale.US)

  val title = config.getString("title")
  val lineColor = config.getString("lineColor")
  val lineWidth = config.getInt("lineWidth")
  val maxPointsPerLine = config.getInt("maxPointsPerLine")
  val dataRepo = config.getString("dataRepo")

  val tmpDir = new File("tmp")
  val outDir = new File("out")
  Files.createDirectories(outDir.toPath)

  Git.cloneRepository.setURI(dataRepo).setDirectory(tmpDir).call
  Generate.process(tmpDir, outDir, title, lineColor, lineWidth, maxPointsPerLine)
}
