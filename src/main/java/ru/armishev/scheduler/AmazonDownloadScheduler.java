package ru.armishev.scheduler;

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
    private final AmazonObjectJPA amazonObjectJPA;
    private final IAmazonEntity amazonEntity;

    private final Logger logger = LoggerFactory.getLogger(AmazonDownloadScheduler.class);

    @Autowired
    public AmazonDownloadScheduler(AmazonObjectJPA amazonObjectJPA, IAmazonEntity amazonEntity) {
        this.amazonObjectJPA = amazonObjectJPA;
        this.amazonEntity = amazonEntity;
    }

    @Scheduled(fixedDelay = 5000)
    public void myScheduler() {
        List<AmazonObjectEntity> listForSave = amazonEntity.getList();

        if (!listForSave.isEmpty()) {
            amazonObjectJPA.saveAll(listForSave);
        }

        logger.info("Download amazon");
    }
}
