import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";

export const codeBucket = new aws.s3.Bucket("swisshikes-generate-code");

export const codeJarObject = new aws.s3.BucketObject("codeJar", {
    key: "swisshikes-generate.jar",
    bucket: codeBucket.id,
    source: new pulumi.asset.FileAsset("../target/scala-2.13/swisshikes-generate.jar")
});
