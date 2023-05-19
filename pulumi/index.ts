import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";

import {dataBucket} from './src/s3-data';
import {lambdaFunctionUrl} from './src/lambda';

export const bucketName = dataBucket.id;
export const bucketDomainName = dataBucket.bucketDomainName;
export const functionUrl = lambdaFunctionUrl.functionUrl
