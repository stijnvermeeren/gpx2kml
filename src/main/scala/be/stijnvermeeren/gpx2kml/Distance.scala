package be.stijnvermeeren.gpx2kml

import scala.annotation.tailrec

object Distance {
  /**
   * the distance of C from the line connecting A and B
   */
  def distanceLinePoint(a: Coord, b: Coord, c: Coord): Double = {
    val num = math.abs((b.lon - a.lon) * (a.lat - c.lat) - (a.lon - c.lon) * (b.lat - a.lat))

    num / distance(a, b)
  }

  def distance(a: Coord, b: Coord): Double = {
    math.sqrt(math.pow(b.lon - a.lon, 2) + math.pow(b.lat - a.lat, 2))
  }

  /**
   * is c roughly between a and b?
   */
  def isBetween(a: Coord, b: Coord, c: Coord): Boolean = {
    val distanceAB = distance(a, b)
    distance(a, c) < distanceAB && distance(b, c) < distanceAB
  }

  @tailrec
  def removeRedundant(points: Seq[Coord], acc: Seq[Coord] = Seq.empty): Seq[Coord] = {
    if (points.length < 3) {
      acc ++ points
    } else {
      val numberOfRedundantPoints = collectRedundant(points)

      removeRedundant(points.drop(1 + numberOfRedundantPoints), acc :+ points.head)
    }
  }

  @tailrec
  def collectRedundant(points: Seq[Coord], step: Int = 0): Int = {
    if (points.length < step + 3) {
      step
    } else {
      val a = points.head
      val middle = points.slice(1, step + 2)
      val b = points(step + 2)

      if (middle.forall(point => isBetween(a, b, point) && distanceLinePoint(a, b, point) < 0.00001)) {
        collectRedundant(points, step + 1)
      } else {
        step
      }
    }
  }
}
