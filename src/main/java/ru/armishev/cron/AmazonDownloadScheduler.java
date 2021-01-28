package ru.armishev.cron;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.armishev.service.IAmazonEntitySync;

@Component
public class AmazonDownloadScheduler {
    private final IAmazonEntitySync amazonEntitySync;

    @Autowired
    public AmazonDownloadScheduler(IAmazonEntitySync amazonEntitySync) {
        this.amazonEntitySync = amazonEntitySync;
    }

    @Scheduled(fixedDelay = 60000)
    public void myScheduler() {
        amazonEntitySync.updateList();
    }
}
