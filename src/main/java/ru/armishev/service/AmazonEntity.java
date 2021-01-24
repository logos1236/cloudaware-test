package ru.armishev.service;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.entity.AmazonObjectOwnerEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/*
Для конвертации объектов из Amazon в объекты приложения
*/
@Service
public class AmazonEntity implements IAmazonEntity {
    private final AmazonService amazonService;

    @Autowired
    public AmazonEntity(AmazonService amazonService) {
        this.amazonService = amazonService;
    }

    @Override
    public List<AmazonObjectEntity> getList() {
        ObjectListing objectListing = amazonService.getListObjectSummary();
        List<AmazonObjectEntity> result = new ArrayList<>();

        if (!objectListing.getObjectSummaries().isEmpty()) {
            objectListing.getObjectSummaries().stream()
                    .parallel()
                    .map((p)->{ return AmazonEntity.convertS3ObjectSummary(p);})
                    .collect(Collectors.toList());
        }

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
