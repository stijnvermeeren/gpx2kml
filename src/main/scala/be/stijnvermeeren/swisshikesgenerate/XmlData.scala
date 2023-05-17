package be.stijnvermeeren.swisshikesgenerate

import scala.xml.Elem

final case class XmlData(xml: Elem, latestDate: Option[String])
