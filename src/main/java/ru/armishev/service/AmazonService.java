package ru.armishev.service;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.stereotype.Service;

@Service
public class AmazonService implements IAmazonService {
    private Regions clientRegion = Regions.US_EAST_1;
    private String bucketName = "cloudaware-test";
    private final AmazonS3 s3Client;

    public AmazonService() {
        s3Client = AmazonS3ClientBuilder.standard()
                //.withCredentials(new ProfileCredentialsProvider())
                .withRegion(clientRegion)
                .build();
    }

    @Override
    public ObjectListing getListObjectSummary() {
        // Because the CreateBucketRequest object doesn't specify a region, the
        // bucket is created in the region specified in the client.
        if (!s3Client.doesBucketExistV2(bucketName)) {
            throw new RuntimeException("Bucket doesn't exist");
        }

        /*ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(3);
        ListObjectsV2Result result;
        result = s3Client.listObjects(req);*/

        ListObjectsRequest req = new ListObjectsRequest().withBucketName(bucketName).withMaxKeys(1);
        ObjectListing result;
        result = s3Client.listObjects(req);

        return result;
    }
}
