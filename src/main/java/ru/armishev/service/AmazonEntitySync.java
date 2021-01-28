package ru.armishev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.armishev.cron.AmazonDownloadScheduler;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.jpa.AmazonObjectJPA;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
Синхронизация объектов из Amazon с БД
*/
@Service
@Primary
public class AmazonEntitySync implements IAmazonEntitySync {
    private final AmazonService amazonService;
    private final AmazonObjectJPA amazonObjectJPA;
    private final Logger logger = LoggerFactory.getLogger(AmazonDownloadScheduler.class);

    @Autowired
    public AmazonEntitySync(AmazonService amazonService, AmazonObjectJPA amazonObjectJPA) {
        this.amazonService = amazonService;
        this.amazonObjectJPA = amazonObjectJPA;
    }

    @Override
    public void updateList() {
        List<AmazonObjectEntity> rawList = amazonService.getListAmazonObjectEntity();
        List<AmazonObjectEntity> currentDatabaseList = amazonObjectJPA.findAll();

        addOrUpdateObjectInDB(rawList, currentDatabaseList);
        deleteNotExistedObjectInDatabase(rawList, currentDatabaseList);
    }

    /*
    Обновляем в локальной БД информацию о файлах в Amazon
    */
    private void addOrUpdateObjectInDB(List<AmazonObjectEntity> rawList, List<AmazonObjectEntity> currentDatabaseList) {
        List<AmazonObjectEntity> listForAddOrUpdate = new ArrayList<>();

        if (rawList.isEmpty()) {
            return;
        }

        if (!currentDatabaseList.isEmpty()) {
            listForAddOrUpdate = rawList.
                    stream().
                    filter(rawObject -> {
                        boolean needAddObj = false;
                        int indexOfDatabaseObj = currentDatabaseList.indexOf(rawObject);

                        if (indexOfDatabaseObj < 0) {
                            needAddObj = true;
                        } else {
                            AmazonObjectEntity currentDatabaseObject = currentDatabaseList.get(indexOfDatabaseObj);
                            if (currentDatabaseObject.getLastModified().compareTo(rawObject.getLastModified()) != 0) {
                                needAddObj = true;
                            }
                        }

                        return needAddObj;
                    }).
                    collect(Collectors.toList());
        } else {
            listForAddOrUpdate = rawList;
        }

        if (!listForAddOrUpdate.isEmpty()) {
            amazonObjectJPA.saveAll(listForAddOrUpdate);

            logger.info("rawList: "+rawList.size());
            logger.info("currentDatabaseList: "+currentDatabaseList.size());
            logger.info("Save new objects to database: "+listForAddOrUpdate.size());
        }
    }

    /*
    Удаляем из локальной БД информацию о файлах, которых больше нет в Amazon
    */
    private void deleteNotExistedObjectInDatabase(List<AmazonObjectEntity> rawList, List<AmazonObjectEntity> currentDatabaseList) {
        List<AmazonObjectEntity> listForDelete = new ArrayList<>();

        if (currentDatabaseList.isEmpty()) {
            return;
        }

        if (!rawList.isEmpty()) {
            listForDelete = currentDatabaseList.stream()
                    .filter(aObject -> {
                        return !rawList.contains(aObject);
                    })
                    .collect(Collectors.toList());
        } else {
            listForDelete = currentDatabaseList;
        }

        if (!listForDelete.isEmpty()) {
            amazonObjectJPA.deleteAll(listForDelete);

            logger.info("Delete not existed in s3 objects from database: "+listForDelete.size());
        }
    }
}
