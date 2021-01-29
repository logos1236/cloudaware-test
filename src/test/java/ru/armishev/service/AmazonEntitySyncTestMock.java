package ru.armishev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.armishev.cron.AmazonDownloadScheduler;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.entity.GrantEntity;
import ru.armishev.entity.OwnerEntity;
import ru.armishev.entity.VersionEntity;
import ru.armishev.jpa.AmazonObjectJPA;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AmazonEntitySyncTestMock implements IAmazonEntitySync {
    private final AmazonObjectJPA amazonObjectJPA;
    private final Logger logger = LoggerFactory.getLogger(AmazonDownloadScheduler.class);
    private static final long cm = System.currentTimeMillis();
    private static boolean isAlreadyAdd = false;

    private static OwnerEntity ownerEntity = new OwnerEntity();

    private static VersionEntity.VersionPK versionPK = new VersionEntity.VersionPK();
    private static VersionEntity versionEntity = new VersionEntity();
    private static VersionEntity.VersionPK versionPK1 = new VersionEntity.VersionPK();
    private static VersionEntity versionEntity1 = new VersionEntity();

    private static GrantEntity grantEntity = new GrantEntity();
    private static GrantEntity grantEntity1 = new GrantEntity();

    private static AmazonObjectEntity amazonObjectEntity1 = new AmazonObjectEntity();
    private static AmazonObjectEntity amazonObjectEntity2 = new AmazonObjectEntity();
    private static AmazonObjectEntity amazonObjectEntity3 = new AmazonObjectEntity();
    {
        ownerEntity.setKey("14fbada9d6aac53a2d851e6c777ffea7cd9ac4d213bee68af9f5d9b247c20c04");
        ownerEntity.setDisplayName("malammik");

        versionPK.setKey("14fbada9d6aac53a2d851e6c777ffea7cd9ac4d213bee68af9f5d9b247c20c04");
        versionPK.setVersion("Test Version");
        versionPK1.setKey("test");
        versionPK1.setVersion("Test Version 2");

        versionEntity.setVersionPK(versionPK);
        versionEntity.setOwner(ownerEntity);
        versionEntity1.setVersionPK(versionPK1);
        versionEntity1.setOwner(ownerEntity);

        grantEntity.setKey("14fbada9d6aac53a2d851e6c777ffea7cd9ac4d213bee68af9f5d9b247c20c04");
        grantEntity.setPermission("FULL_CONTROL");
        grantEntity1.setKey("14fbada9d6aac53a2d851e6c777ffea7cd9ac4d213bee68af9f5d9b247c20c04");
        grantEntity1.setPermission("FULL_CONTROL");

        amazonObjectEntity1.setKey("file_2015-08-06.txt");
        amazonObjectEntity1.setLastModified(new Date(System.currentTimeMillis()));
        amazonObjectEntity1.setETag("&quot;090228db8da1203d89d73341c95932b4&quot;");
        amazonObjectEntity1.setSize(12L);
        amazonObjectEntity1.setStorageClass("STANDARD");
        amazonObjectEntity1.setOwner(ownerEntity);
        amazonObjectEntity1.setGrants(new ArrayList<GrantEntity>(){{add(grantEntity);}});
        amazonObjectEntity1.setVersions(new ArrayList<VersionEntity>(){{add(versionEntity);}});

        amazonObjectEntity2.setKey("2");
        amazonObjectEntity2.setLastModified(new Date(cm));
        amazonObjectEntity2.setETag("ETag");
        amazonObjectEntity2.setSize(12L);
        amazonObjectEntity2.setStorageClass("Test Static");
        amazonObjectEntity2.setOwner(ownerEntity);
        amazonObjectEntity2.setGrants(new ArrayList<GrantEntity>(){{add(grantEntity1);}});
        amazonObjectEntity2.setVersions(new ArrayList<VersionEntity>(){{add(versionEntity1);}});

        amazonObjectEntity3.setKey("3");
        amazonObjectEntity3.setLastModified(new Date(cm));
        amazonObjectEntity3.setETag("ETag");
        amazonObjectEntity3.setSize(12L);
        amazonObjectEntity3.setStorageClass("Test Static");
        amazonObjectEntity3.setOwner(ownerEntity);
        amazonObjectEntity3.setGrants(new ArrayList<GrantEntity>(){{add(grantEntity1);}});
        amazonObjectEntity3.setVersions(new ArrayList<VersionEntity>(){{add(versionEntity1);}});
    }

    @Autowired
    public AmazonEntitySyncTestMock(AmazonObjectJPA amazonObjectJPA) {
        this.amazonObjectJPA = amazonObjectJPA;
    }

    @Override
    public void syncList() {
        List<AmazonObjectEntity> rawList = new ArrayList<>();
        rawList.add(amazonObjectEntity1);
        rawList.add(amazonObjectEntity2);
        rawList.add(amazonObjectEntity3);

        List<AmazonObjectEntity> currentDatabaseList = amazonObjectJPA.findAll();

        addOrUpdateObjFromDatabase(rawList, currentDatabaseList);
        deleteNotExistedObjFromDatabase(rawList, currentDatabaseList);
    }

    public void updateList2Elements() {
        List<AmazonObjectEntity> rawList = new ArrayList<>();
        rawList.add(amazonObjectEntity1);
        rawList.add(amazonObjectEntity2);

        List<AmazonObjectEntity> currentDatabaseList = amazonObjectJPA.findAll();

        addOrUpdateObjFromDatabase(rawList, currentDatabaseList);
        deleteNotExistedObjFromDatabase(rawList, currentDatabaseList);
    }

    private void addOrUpdateObjFromDatabase(List<AmazonObjectEntity> rawList, List<AmazonObjectEntity> currentDatabaseList) {
        List<AmazonObjectEntity> listForAddOrUpdate = rawList.stream()
                .filter(rawObject -> {
                    boolean needAddObj = false;

                    int indexOfDatabaseObj = currentDatabaseList.indexOf(rawObject);
                    logger.info("indexOfDatabaseObj: "+indexOfDatabaseObj);

                    if (indexOfDatabaseObj < 0) {
                        needAddObj = true;
                    } else {
                        AmazonObjectEntity currentDatabaseObject = currentDatabaseList.get(indexOfDatabaseObj);
                        if (currentDatabaseObject.getLastModified().compareTo(rawObject.getLastModified()) != 0) {
                            logger.info(currentDatabaseObject.getLastModified()+" : "+rawObject.getLastModified());
                            needAddObj = true;
                        }
                    }

                    return needAddObj;
                })
                .collect(Collectors.toList());

        if (!listForAddOrUpdate.isEmpty()) {
            amazonObjectJPA.saveAll(listForAddOrUpdate);

            logger.info("Save new objects to database: "+listForAddOrUpdate.size());
        }
    }

    private void deleteNotExistedObjFromDatabase(List<AmazonObjectEntity> rawList, List<AmazonObjectEntity> currentDatabaseList) {
        List<AmazonObjectEntity> listForDelete = currentDatabaseList.stream()
                .filter(aObject -> {
                    return !rawList.contains(aObject);
                })
                .collect(Collectors.toList());

        if (!listForDelete.isEmpty()) {
            amazonObjectJPA.deleteAll(listForDelete);

            logger.info("Delete not existed in s3 objects from database: "+listForDelete.size());
        }
    }
}
