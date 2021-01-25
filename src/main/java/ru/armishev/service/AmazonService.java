package ru.armishev.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AmazonService implements IAmazonService {
    private Regions clientRegion = Regions.US_EAST_1;
    private String bucketName = "cloudaware-test";
    private final AmazonS3 s3Client;
    private final Logger logger = LoggerFactory.getLogger(AmazonService.class);

    public AmazonService() {
        s3Client = AmazonS3ClientBuilder.standard()
                //.withCredentials(new ProfileCredentialsProvider())
                .withRegion(clientRegion)
                .build();
    }

    @Override
    public ObjectListing getListObjectSummary() {
        /* Because the CreateBucketRequest object doesn't specify a region, the
        * bucket is created in the region specified in the client.
         */
        if (!s3Client.doesBucketExistV2(bucketName)) {
            logger.error("Bucket doesn't exist");
            throw new RuntimeException("Bucket doesn't exist");
        }

        /*ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(3);
        ListObjectsV2Result result;
        result = s3Client.listObjects(req);*/

        ListObjectsRequest req = new ListObjectsRequest().withBucketName(bucketName).withMaxKeys(1);
        ObjectListing result;
        result = s3Client.listObjects(req);


        /*
        !!!!!!!!!!!
        Always check the ObjectListing.isTruncated() method to see if the returned listing is complete,
        or if callers need to make additional calls to get more results.
        Alternatively, use the AmazonS3Client.listNextBatchOfVersions(VersionListing)
        method as an easy way to get the next page of object listings.
         */

        /*do {
            result = s3Client.listObjectsV2(req);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
            }
            // If there are more than maxKeys keys in the bucket, get a continuation token
            // and list the next objects.
            String token = result.getNextContinuationToken();
            System.out.println("Next Continuation Token: " + token);
            req.setContinuationToken(token);
            } while (result.isTruncated());
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }*/

        return result;
    }
}
