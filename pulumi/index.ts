import {dataBucket} from './src/s3-data';
import {lambdaFunctionUrl} from './src/lambda';
import {codeJarObject} from "./src/s3-code";

export const bucketName = dataBucket.id;
export const bucketDomainName = dataBucket.bucketDomainName;
export const functionUrl = lambdaFunctionUrl.functionUrl
export const codeJarVersionId = codeJarObject.versionId