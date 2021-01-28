package ru.armishev;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.util.Iterator;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        Regions clientRegion = Regions.US_EAST_1;
        String bucketName = "cloudaware-test";

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();

            // Retrieve the list of versions. If the bucket contains more versions
            // than the specified maximum number of results, Amazon S3 returns
            // one page of results per request.

			ListVersionsRequest request = new ListVersionsRequest()
					.withBucketName(bucketName)
					.withMaxResults(10)
			        .withPrefix("file_2015-08-06.txt");

			VersionListing version_listing = s3Client.listVersions(request);
			while (true) {
				for (Iterator<?> iterator = version_listing.getVersionSummaries().iterator(); iterator.hasNext(); ) {
					S3VersionSummary vs = (S3VersionSummary) iterator.next();

					System.out.println(vs.getKey()+" : "+vs.getOwner()+" : "+vs.getVersionId()+" : "+vs.isLatest());
				}

				if (version_listing.isTruncated()) {
					version_listing = s3Client.listNextBatchOfVersions(version_listing);
				} else {
					break;
				}
			}

            /*try {
                AccessControlList acl = s3Client.getObjectAcl(bucketName, "file_2015-08-10.txt");
                List<Grant> grants = acl.getGrantsAsList();
                for (Grant grant : grants) {
                    System.out.format("  %s: %s\n", grant.getGrantee().getIdentifier(), grant.getPermission().toString());
                }
            } catch (AmazonServiceException e) {
                System.err.println(e.getErrorMessage());
                System.exit(1);
            }*/


        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }
}
