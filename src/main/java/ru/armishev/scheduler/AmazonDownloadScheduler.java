package ru.armishev.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AmazonDownloadScheduler {
    @Scheduled(fixedDelay = 5000)
    public void myScheduler() {
        System.out.println("Test print");
    }
}
