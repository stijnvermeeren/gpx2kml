package be.stijnvermeeren.gpx2kml

class MaxPointsEnforcer(maxPoints: Int) {
  val defaultMinDeviation: Double = 0.00001
  val recursiveFactor: Double = 1.2

  def reducePoints(points: Seq[Coord]): Seq[Coord] = {
    LazyList.iterate(defaultMinDeviation)(_ * recursiveFactor)
      .map(minDeviation => Distance.removeRedundant(points, minDeviation))
      .find(_.length <= maxPoints)
      .getOrElse(Seq.empty)
  }
}
