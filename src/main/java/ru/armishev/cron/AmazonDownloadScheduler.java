package ru.armishev.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.jpa.AmazonObjectJPA;
import ru.armishev.service.IAmazonEntity;

import java.util.List;

@Component
public class AmazonDownloadScheduler {
    private final IAmazonEntity amazonEntity;

    private final Logger logger = LoggerFactory.getLogger(AmazonDownloadScheduler.class);

    @Autowired
    public AmazonDownloadScheduler(IAmazonEntity amazonEntity) {
        this.amazonEntity = amazonEntity;
    }

    @Scheduled(fixedDelay = 5000)
    public void myScheduler() {
        amazonEntity.updateList();
    }
}
