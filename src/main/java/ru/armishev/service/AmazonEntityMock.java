package ru.armishev.service;

import com.amazonaws.services.s3.model.ObjectListing;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.entity.AmazonObjectOwnerEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Primary
public class AmazonEntityMock implements IAmazonEntity {
    @Override
    public List<AmazonObjectEntity> getList() {
        List<AmazonObjectEntity> result = new ArrayList<>();

        AmazonObjectOwnerEntity amazonObjectOwnerEntity = new AmazonObjectOwnerEntity();
        amazonObjectOwnerEntity.setId("12");
        amazonObjectOwnerEntity.setDisplayName("amazonObjectOwnerEntity");

        AmazonObjectEntity newAmazonObjectEntity = new AmazonObjectEntity();
        newAmazonObjectEntity.setKey("1");
        newAmazonObjectEntity.setLastModified(new Date());
        newAmazonObjectEntity.setETag("ETag");
        newAmazonObjectEntity.setSize(12L);
        newAmazonObjectEntity.setStorageClass("Test");
        newAmazonObjectEntity.setOwner(amazonObjectOwnerEntity);

        result.add(newAmazonObjectEntity);

        return result;
    }
}
