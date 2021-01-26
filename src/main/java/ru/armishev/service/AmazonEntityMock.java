package ru.armishev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.armishev.cron.AmazonDownloadScheduler;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.entity.AmazonObjectOwnerEntity;
import ru.armishev.jpa.AmazonObjectJPA;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Primary
public class AmazonEntityMock implements IAmazonEntity {
    private final AmazonService amazonService;
    private final AmazonObjectJPA amazonObjectJPA;
    private final Logger logger = LoggerFactory.getLogger(AmazonDownloadScheduler.class);
    private static final long cm = System.currentTimeMillis();
    private static boolean isAlreadyAdd = false;

    @Autowired
    public AmazonEntityMock(AmazonService amazonService, AmazonObjectJPA amazonObjectJPA) {
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

            logger.info("Delete not existed in s3 objects from database: "+listForDelete.size());
        }
    }

    private List<AmazonObjectEntity> getListFromAmazon() {
        List<AmazonObjectEntity> result = new ArrayList<>();

        AmazonObjectOwnerEntity amazonObjectOwnerEntity = new AmazonObjectOwnerEntity();
        amazonObjectOwnerEntity.setId("14fbada9d6aac53a2d851e6c777ffea7cd9ac4d213bee68af9f5d9b247c20c04");
        amazonObjectOwnerEntity.setDisplayName("malammik");

        //
        AmazonObjectEntity newAmazonObjectEntity = new AmazonObjectEntity();
        newAmazonObjectEntity.setKey("file_2015-08-06.txt");
        newAmazonObjectEntity.setLastModified(new Date(System.currentTimeMillis()));
        newAmazonObjectEntity.setETag("&quot;090228db8da1203d89d73341c95932b4&quot;");
        newAmazonObjectEntity.setSize(12L);
        newAmazonObjectEntity.setStorageClass("STANDARD");
        newAmazonObjectEntity.setOwner(amazonObjectOwnerEntity);
        result.add(newAmazonObjectEntity);

        //
        AmazonObjectEntity newAmazonObjectEntityStatic = new AmazonObjectEntity();
        newAmazonObjectEntityStatic.setKey("3");
        newAmazonObjectEntityStatic.setLastModified(new Date(cm));
        newAmazonObjectEntityStatic.setETag("ETag");
        newAmazonObjectEntityStatic.setSize(12L);
        newAmazonObjectEntityStatic.setStorageClass("Test Static");
        newAmazonObjectEntityStatic.setOwner(amazonObjectOwnerEntity);
        result.add(newAmazonObjectEntityStatic);

        //
        if (!isAlreadyAdd) {
            AmazonObjectEntity newAmazonObjectEntityDeleted = new AmazonObjectEntity();
            newAmazonObjectEntityDeleted.setKey("2");
            newAmazonObjectEntityDeleted.setLastModified(new Date(cm));
            newAmazonObjectEntityDeleted.setETag("ETag");
            newAmazonObjectEntityDeleted.setSize(12L);
            newAmazonObjectEntityDeleted.setStorageClass("Test Static");
            newAmazonObjectEntityDeleted.setOwner(amazonObjectOwnerEntity);
            result.add(newAmazonObjectEntityDeleted);

            isAlreadyAdd = true;
        }


        return result;
    }
}
