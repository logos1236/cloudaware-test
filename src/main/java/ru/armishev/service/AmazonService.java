package ru.armishev.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.entity.GrantEntity;
import ru.armishev.entity.OwnerEntity;
import ru.armishev.entity.VersionEntity;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AmazonService implements IAmazonService {
    private final Regions clientRegion;
    private final String bucketName;
    private final static int MAX_DOWNLOAD_CNT = 1;
    private final static int MAX_DOWNLOAD_CNT_VERSION = 1;

    private final Logger logger = LoggerFactory.getLogger(AmazonService.class);

    public AmazonService(@Value("${amazon.bucket_name}") String bucketName, @Value("${amazon.clientRegion}") String clientRegion) {
        this.bucketName = bucketName;
        this.clientRegion = Regions.fromName(clientRegion);
    }

    @Override
    public List<AmazonObjectEntity> getListAmazonObjectEntity() {
        List<AmazonObjectEntity> resultList = new ArrayList<>();
        AmazonS3 s3Client;
        ListObjectsV2Request req;
        ListObjectsV2Result result;

        try {
            s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();

            if (!s3Client.doesBucketExistV2(bucketName)) {
                logger.error("Bucket doesn't exist");
                throw new RuntimeException("Bucket doesn't exist");
            }

            req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(MAX_DOWNLOAD_CNT);
            req.setFetchOwner(true);
            //do {
                result = s3Client.listObjectsV2(req);
                result.getObjectSummaries().
                        stream().
                        map((S3ObjectSummary)->{
                            List<Grant> grants = getListAmazonObjectGrants(S3ObjectSummary.getKey());
                            List<S3VersionSummary> versions = getListAmazonObjectVersions(S3ObjectSummary.getKey());

                            return convertS3ObjectSummary(S3ObjectSummary, grants, versions);
                        }).
                        collect(Collectors.toCollection(() -> resultList));

                String token = result.getNextContinuationToken();
                req.setContinuationToken(token);
            //} while (result.isTruncated());

            logger.info("Download S3ObjectSummary from Amazon");
            logger.info("resultList: "+resultList.size());
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            logger.error(e.getErrorCode()+" : "+e.getErrorMessage());
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            logger.error(e.getMessage());
        }

        return resultList;
    }

    private List<S3VersionSummary> getListAmazonObjectVersions(String fileName) {
        List<S3VersionSummary> resultList = new ArrayList<>();
        AmazonS3 s3Client;

        try {
            s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();

            if (!s3Client.doesBucketExistV2(bucketName)) {
                logger.error("Bucket doesn't exist");
                throw new RuntimeException("Bucket doesn't exist");
            }

            ListVersionsRequest req = new ListVersionsRequest().
                    withBucketName(bucketName).
                    withMaxResults(MAX_DOWNLOAD_CNT_VERSION).
                    withPrefix(fileName);

            VersionListing result = s3Client.listVersions(req);

            while (true) {
                result.getVersionSummaries().
                        stream().
                        collect(Collectors.toCollection(() -> resultList));

                if (result.isTruncated()) {
                    result = s3Client.listNextBatchOfVersions(result);
                } else {
                    break;
                }
            }

            logger.info("Download S3ObjectSummary from Amazon");
            logger.info("resultList: "+resultList.size());
        } catch (AmazonServiceException e) {
            logger.error(e.getErrorCode()+" : "+e.getErrorMessage());
        } catch (SdkClientException e) {
            logger.error(e.getMessage());
        }

        return resultList;
    }

    private List<Grant> getListAmazonObjectGrants(String fileName) {
        AmazonS3 s3Client;
        List<Grant> grants = new ArrayList<>();

        try {
            s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();

            AccessControlList acl = s3Client.getObjectAcl(bucketName, fileName);
            grants = acl.getGrantsAsList();
            for (Grant grant : grants) {
                System.out.format("  %s: %s\n", grant.getGrantee().getIdentifier(), grant.getPermission().toString());
            }
        } catch (AmazonServiceException e) {
            logger.error(e.getErrorMessage());
        }

        return grants;
    }

    /*
    Конвертируем S3ObjectSummary (из Amazon) в AmazonObjectEntity
    */
    private static AmazonObjectEntity convertS3ObjectSummary(S3ObjectSummary objectSummary,
                                                             List<Grant> grantList,
                                                             List<S3VersionSummary> versionList) {
        AmazonObjectEntity amazonObjectEntity = new AmazonObjectEntity();

        amazonObjectEntity.setKey(objectSummary.getKey());
        amazonObjectEntity.setLastModified(objectSummary.getLastModified());
        amazonObjectEntity.setETag(objectSummary.getETag());
        amazonObjectEntity.setSize(objectSummary.getSize());
        amazonObjectEntity.setOwner(convertS3ObjectSummaryOwner(objectSummary.getOwner()));
        amazonObjectEntity.setStorageClass(objectSummary.getStorageClass());
        amazonObjectEntity.setGrants(convertS3ObjectGrantList(grantList));
        amazonObjectEntity.setVersions(convertS3ObjectVersionList(versionList));

        return amazonObjectEntity;
    }

    /*
    Конвертируем Owner объекта S3ObjectSummary (из Amazon) в OwnerEntity
     */
    private static OwnerEntity convertS3ObjectSummaryOwner(com.amazonaws.services.s3.model.Owner objectOwner) {
        OwnerEntity ownerEntity = null;

        if (objectOwner != null) {
            ownerEntity = new OwnerEntity();
            ownerEntity.setKey(objectOwner.getId());
            ownerEntity.setDisplayName(objectOwner.getDisplayName());
        }

        return ownerEntity;
    }

    /*
    Конвертируем Grant объекта S3ObjectSummary (из Amazon) в GrantEntity
     */
    private static List<GrantEntity> convertS3ObjectGrantList(List<Grant> grants) {
        List<GrantEntity> result = new ArrayList<>();

        if (!grants.isEmpty()) {
            grants.
                    stream().
                    map((grant)->{
                        return convertS3ObjectGrant(grant);
                    }).
                    collect(Collectors.toCollection(() -> result));
        }

        return result;
    }

    private static GrantEntity convertS3ObjectGrant(Grant grant) {
        GrantEntity grantEntity = new GrantEntity();
        grantEntity.setKey(grant.getGrantee().getIdentifier());
        grantEntity.setName(grant.getPermission().toString());

        return grantEntity;
    }

    /*
    Конвертируем S3VersionSummary (из Amazon) в VersionEntity
    */
    private static List<VersionEntity> convertS3ObjectVersionList(List<S3VersionSummary> s3VersionSummaryList) {
        List<VersionEntity> result = new ArrayList<>();

        if (!s3VersionSummaryList.isEmpty()) {
            s3VersionSummaryList.
                    stream().
                    map((s3VersionSummary)->{
                        return convertS3ObjectVersion(s3VersionSummary);
                    }).
                    collect(Collectors.toCollection(() -> result));
        }

        return result;
    }
    private static VersionEntity convertS3ObjectVersion(S3VersionSummary s3VersionSummary) {
        VersionEntity.VersionPK versionPK = new VersionEntity.VersionPK();
        VersionEntity versionEntity = new VersionEntity();

        versionPK.setKey(s3VersionSummary.getKey());
        versionPK.setVersion(s3VersionSummary.getVersionId());

        versionEntity.setVersionPK(versionPK);
        versionEntity.setLatest(s3VersionSummary.isLatest());
        versionEntity.setOwner(convertS3ObjectSummaryOwner(s3VersionSummary.getOwner()));
        versionEntity.setDeleted(s3VersionSummary.isDeleteMarker());

        return versionEntity;
    }
}
