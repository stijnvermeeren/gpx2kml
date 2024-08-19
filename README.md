# Stijn's hikes in Switzerland: generate script

A Scala script that processes the `.gpx` tracks and associated metadata from the [stijnvermeeren/swisshikes-data](https://github.com/stijnvermeeren/swisshikes-data) repository, collects this data into `.kml` files (one per year) and uploads those to S3, so that they can be loaded and displayed on the _Stijn's hikes in Switzerland_ webpage ([stijnvermeeren.be/swisshikes](https://stijnvermeeren.be/swisshikes)).

When processing the GPS tracks, the script also applies some compression (removing redundant points, dropping excessively precise decimals) to reduce the file size.

This repository also includes code for deploying this script to AWS with [Pulumi](https://www.pulumi.com/). The script will be run as a Lambda and the required S3 buckets (one to contain the `.jar` file of the script, and one for the output files) will be created. To do this deployment, first run `sbt assembly` and then `pulumi up` (in the [pulumi](./pulumi) directory). This assumes that [sbt](https://www.scala-sbt.org/) and [Pulumi](https://www.pulumi.com/) are installed.

Alternative, you can run the script locally with  `sbt run`, after setting the AWS region and S3 bucket as environment variables `AWS_REGION` and `AWS_BUCKET`.

Configuration is done using [Lightbend Config](https://github.com/lightbend/config). See [reference.conf](src/main/resources/reference.conf) for default values.
