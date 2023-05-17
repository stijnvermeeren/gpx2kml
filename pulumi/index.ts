import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";
import * as awsx from "@pulumi/awsx";

const bucket = new aws.s3.Bucket("swisshikes-kml");

const bucketPublicAccessBlock = new aws.s3.BucketPublicAccessBlock("bucketPublicAccessBlock", {
    bucket: bucket.id,
    blockPublicPolicy: false,
    restrictPublicBuckets: false,
});

const bucketCorsConfigurationV2 = new aws.s3.BucketCorsConfigurationV2("bucketCorsConfiguration", {
    bucket: bucket.id,
    corsRules: [
        {
            allowedMethods: ["GET", "HEAD"],
            allowedOrigins: ["*"]
        }
    ],
});

const bucketPolicy = new aws.s3.BucketPolicy("bucketPolicy", {
    bucket: bucket.bucket,
    policy: {
        Version: "2012-10-17",
        Statement: [{
            Effect: "Allow",
            Principal: "*",
            Action: [
                "s3:GetObject"
            ],
            Resource: [
                pulumi.interpolate`${bucket.arn}/*`
            ]
        }]
    }
});

export const bucketName = bucket.id;
