package io.ducommun.mariokart.teamsapi

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder

private val awsStaticCredentialsProvider = AWSStaticCredentialsProvider(
    BasicAWSCredentials("","")
)

internal val client = AmazonS3ClientBuilder
    .standard()
    .withRegion(Regions.US_WEST_2)
    .withCredentials(awsStaticCredentialsProvider)
    .build()