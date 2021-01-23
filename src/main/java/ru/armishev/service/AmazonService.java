package ru.armishev.service;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.stereotype.Service;

@Service
public class AmazonService {
    Regions clientRegion = Regions.US_EAST_1;
    String bucketName = "cloudaware-test";

    public ObjectListing list() {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                //.withCredentials(new ProfileCredentialsProvider())
                .withRegion(clientRegion)
                .build();

        // Because the CreateBucketRequest object doesn't specify a region, the
        // bucket is created in the region specified in the client.
        if (!s3Client.doesBucketExistV2(bucketName)) {

        }
        /*
        return owner as null
        ???
         */
        /*ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(3);
        ListObjectsV2Result result;
        result = s3Client.listObjects(req);*/

        ListObjectsRequest req = new ListObjectsRequest().withBucketName(bucketName).withMaxKeys(1);
        ObjectListing result;
        result = s3Client.listObjects(req);

        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            System.out.println(objectSummary.getKey());
            System.out.println(objectSummary.getLastModified());
            System.out.println(objectSummary.getETag());
            System.out.println(objectSummary.getSize());
            System.out.println(objectSummary.getOwner());
            System.out.println(objectSummary.getStorageClass());

            System.out.println("=============");
            //System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
        }

        return result;
    }
}
