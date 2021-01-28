package ru.armishev.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.armishev.service.IAmazonEntitySync;

@Component
public class AmazonDownloadScheduler {
    private final IAmazonEntitySync amazonEntity;

    private final Logger logger = LoggerFactory.getLogger(AmazonDownloadScheduler.class);

    @Autowired
    public AmazonDownloadScheduler(IAmazonEntitySync amazonEntity) {
        this.amazonEntity = amazonEntity;
    }

    @Scheduled(fixedDelay = 60000)
    public void myScheduler() {
        amazonEntity.updateList();
    }
}
