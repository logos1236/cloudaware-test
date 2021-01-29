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

/*
Подключение к Amazon получение информации файлов
*/
@Service
public class AmazonService implements IAmazonService {
    private static final int MAX_DOWNLOAD_CNT = 5;
    private static final int MAX_DOWNLOAD_CNT_VERSION = 1;

    private final Regions clientRegion;
    private final String bucketName;

    private AmazonS3 s3Client;
    private ListObjectsV2Result loopListObjectsV2Result;
    private ListObjectsV2Request loopListObjectsV2Request;
    private List<String> loopFilesList = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(AmazonService.class);

    public AmazonService(@Value("${amazon.bucket_name}") String bucketName, @Value("${amazon.clientRegion}") String clientRegion) {
        this.bucketName = bucketName;
        this.clientRegion = Regions.fromName(clientRegion);
    }

    /*
    Создаем объект AmazonS3 объект и проверяем, существует ли корзина
    */
    private AmazonS3 s3Client() {
        if (this.s3Client == null) {
            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();

            if (!s3Client.doesBucketExistV2(bucketName)) {
                logger.error("Bucket doesn't exist");
                throw new AmazonServiceException("Bucket doesn't exist");
            }
        }

        return this.s3Client;
    }

    /*
    Цикл скачивания файлов закончен?
    */
    public boolean isLoopEnd() {
        boolean result = false;

        if (loopListObjectsV2Result != null && !loopListObjectsV2Result.isTruncated()) {
            result = true;
        }

        return result;
    }

    /*
    Список имен файлов в корзине, скачанных за один полный цикл
    */
    public List<String> getLoopFilesList() {
        return loopFilesList;
    }

    /*
    Зацикливаем запрос объектов из Amazon
    */
    private void initLoopListObjectsV2Result() {
        if (this.loopListObjectsV2Request == null || !loopListObjectsV2Result.isTruncated()) {
            this.loopListObjectsV2Request = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(MAX_DOWNLOAD_CNT);
            this.loopListObjectsV2Request.setFetchOwner(true);

            loopFilesList = new ArrayList<>();
        }

        loopListObjectsV2Result = s3Client().listObjectsV2(this.loopListObjectsV2Request);
    }

    /*
    Список файлов
    */
    @Override
    public List<AmazonObjectEntity> getListAmazonObjectEntity() {
        List<AmazonObjectEntity> resultList = new ArrayList<>();
        logger.info("Start download S3ObjectSummary from Amazon");

        try {
            initLoopListObjectsV2Result();

            this.loopListObjectsV2Result.getObjectSummaries().
                    stream().
                    map(s3ObjectSummary -> {
                        List<Grant> grants = getListAmazonObjectGrants(s3ObjectSummary.getKey());
                        List<S3VersionSummary> versions = getListAmazonObjectVersions(s3ObjectSummary.getKey());

                        loopFilesList.add(s3ObjectSummary.getKey());

                        return convertS3ObjectSummary(s3ObjectSummary, grants, versions);
                    }).
                    collect(Collectors.toCollection(() -> resultList));

            String token = this.loopListObjectsV2Result.getNextContinuationToken();
            this.loopListObjectsV2Request.setContinuationToken(token);
        } catch (AmazonServiceException e) {
            logger.error(e.getErrorMessage());
        } catch (SdkClientException e) {
            logger.error(e.getMessage());
        }

        return resultList;
    }

    /*
    Список версий файла
    */
    private List<S3VersionSummary> getListAmazonObjectVersions(String fileName) {
        List<S3VersionSummary> resultList = new ArrayList<>();
        logger.info("Download version for file from Amazon");

        try {
            ListVersionsRequest req = new ListVersionsRequest().
                    withBucketName(bucketName).
                    withMaxResults(MAX_DOWNLOAD_CNT_VERSION).
                    withPrefix(fileName);

            VersionListing result = s3Client().listVersions(req);

            while (true) {
                result.getVersionSummaries().
                        stream().
                        collect(Collectors.toCollection(() -> resultList));

                if (result.isTruncated()) {
                    result = s3Client().listNextBatchOfVersions(result);
                } else {
                    break;
                }
            }
        } catch (AmazonServiceException e) {
            logger.error(e.getErrorMessage());
        } catch (SdkClientException e) {
            logger.error(e.getMessage());
        }

        return resultList;
    }

    /*
    Список прав доступа файла
    */
    private List<Grant> getListAmazonObjectGrants(String fileName) {
        List<Grant> grants = new ArrayList<>();
        logger.info("Download Grants for file from Amazon");

        try {
            AccessControlList acl = s3Client().getObjectAcl(bucketName, fileName);
            grants = acl.getGrantsAsList();
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
                    map(grant->{
                        return convertS3ObjectGrant(grant);
                    }).
                    collect(Collectors.toCollection(() -> result));
        }

        return result;
    }
    private static GrantEntity convertS3ObjectGrant(Grant grant) {
        GrantEntity grantEntity = null;

        if (grant != null) {
            grantEntity = new GrantEntity();
            grantEntity.setKey(grant.getGrantee().getIdentifier());
            grantEntity.setPermission(grant.getPermission().toString());
        }

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
                    map(s3VersionSummary->{
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
