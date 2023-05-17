package be.stijnvermeeren.swisshikesgenerate

final case class Metadata(
  latestDate: Option[String],
  files: List[String]
)
