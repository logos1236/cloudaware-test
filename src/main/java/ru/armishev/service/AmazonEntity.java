package ru.armishev.service;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.armishev.cron.AmazonDownloadScheduler;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.entity.AmazonObjectOwnerEntity;
import ru.armishev.jpa.AmazonObjectJPA;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
Для конвертации объектов из Amazon в объекты приложения
*/
@Service
public class AmazonEntity implements IAmazonEntity {
    private final AmazonService amazonService;
    private final AmazonObjectJPA amazonObjectJPA;
    private final Logger logger = LoggerFactory.getLogger(AmazonDownloadScheduler.class);

    @Autowired
    public AmazonEntity(AmazonService amazonService, AmazonObjectJPA amazonObjectJPA) {
        this.amazonService = amazonService;
        this.amazonObjectJPA = amazonObjectJPA;
    }

    @Override
    public void updateList() {
        List<AmazonObjectEntity> rawList = this.getListFromAmazon();
        List<AmazonObjectEntity> currentDatabaseList = amazonObjectJPA.findAll();

        addOrUpdateObjFromDatabase(rawList, currentDatabaseList);
        deleteNotExistedObjFromDatabase(rawList, currentDatabaseList);
    }

    private void addOrUpdateObjFromDatabase(List<AmazonObjectEntity> rawList, List<AmazonObjectEntity> currentDatabaseList) {
        List<AmazonObjectEntity> listForAddOrUpdate = new ArrayList<>();

        listForAddOrUpdate = rawList.stream()
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
        List<AmazonObjectEntity> listForDelete = new ArrayList<>();
        listForDelete = currentDatabaseList.stream()
                .filter(aObject -> {
                    return !rawList.contains(aObject);
                })
                .collect(Collectors.toList());

        if (!listForDelete.isEmpty()) {
            amazonObjectJPA.deleteAll(listForDelete);

            logger.info("Delete not existed in s3 objects from database");
        }
    }

    private List<AmazonObjectEntity> getListFromAmazon() {
        ObjectListing objectListing = amazonService.getListObjectSummary();
        List<AmazonObjectEntity> result = new ArrayList<>();

        if (!objectListing.getObjectSummaries().isEmpty()) {
            objectListing.getObjectSummaries().stream()
                    .parallel()
                    .map((p)->{ return AmazonEntity.convertS3ObjectSummary(p);})
                    .collect(Collectors.toList());
        }

        logger.info("Start download data from Amazon");

        return result;
    }

    /*
    Конвертируем S3ObjectSummary (из Amazon) в AmazonObjectEntity
     */
    private static AmazonObjectEntity convertS3ObjectSummary(S3ObjectSummary objectSummary) {
        AmazonObjectEntity amazonObjectEntity = new AmazonObjectEntity();

        amazonObjectEntity.setKey(objectSummary.getKey());
        amazonObjectEntity.setLastModified(objectSummary.getLastModified());
        amazonObjectEntity.setETag(objectSummary.getETag());
        amazonObjectEntity.setSize(objectSummary.getSize());
        amazonObjectEntity.setOwner(convertS3ObjectSummaryOwner(objectSummary.getOwner()));
        amazonObjectEntity.setStorageClass(objectSummary.getStorageClass());

        return amazonObjectEntity;
    }

    /*
    Конвертируем Owner объекта S3ObjectSummary (из Amazon) в AmazonObjectOwnerEntity
     */
    private static AmazonObjectOwnerEntity convertS3ObjectSummaryOwner(Owner objectOwner) {
        AmazonObjectOwnerEntity amazonObjectOwnerEntity = new AmazonObjectOwnerEntity();
        amazonObjectOwnerEntity.setId(objectOwner.getId());
        amazonObjectOwnerEntity.setDisplayName(objectOwner.getDisplayName());

        return amazonObjectOwnerEntity;
    }
}
