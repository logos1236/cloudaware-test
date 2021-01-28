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
        addOrUpdateObjectInDB(rawList);

        /*
            Если цикл загрузки всех файлов завершился, то находим разницу между
            файлами, загруженными в данном цикле и уже присутствующими в базе
            Если в базе есть файлы, не присутствующие в данном цикле загрузки - удаляем их
         */
        if (amazonService.isLoopEnd()) {
            System.out.println("End cycle");
            List<AmazonObjectEntity> currentDatabaseList = amazonObjectJPA.findAll();
            deleteNotExistedObjectInDatabase(amazonService.getLoopFilesList(), currentDatabaseList);
        }
    }

    /*
    Обновляем в локальной БД информацию о файлах в Amazon
    */
    private void addOrUpdateObjectInDB(List<AmazonObjectEntity> rawList) {
        if (!rawList.isEmpty()) {
            amazonObjectJPA.saveAll(rawList);

            logger.info("rawList: "+rawList.size());
            logger.info("Save new objects to database: "+rawList.size());
        }
    }

    /*
    Удаляем из локальной БД информацию о файлах, которых больше нет в Amazon
    */
    private void deleteNotExistedObjectInDatabase(List<String> loopFilesList, List<AmazonObjectEntity> currentDatabaseList) {
        List<AmazonObjectEntity> listForDelete = new ArrayList<>();

        if (currentDatabaseList.isEmpty()) {
            return;
        }

        if (!loopFilesList.isEmpty()) {
            listForDelete = currentDatabaseList.stream()
                    .filter(aObject -> {
                        return !loopFilesList.contains(aObject.getKey());
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
