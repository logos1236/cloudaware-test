package ru.armishev.service;

import com.amazonaws.services.s3.model.ObjectListing;
import ru.armishev.entity.AmazonObjectEntity;

import java.util.List;

public interface IAmazonEntity {
    void updateList();
}
