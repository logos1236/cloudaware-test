package ru.armishev.service;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import ru.armishev.entity.AmazonObjectEntity;

import java.util.List;

public interface IAmazonService {
    public List<AmazonObjectEntity> getListAmazonObjectEntity();
}
