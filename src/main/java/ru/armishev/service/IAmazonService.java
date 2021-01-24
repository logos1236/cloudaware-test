package ru.armishev.service;

import com.amazonaws.services.s3.model.ObjectListing;

public interface IAmazonService {
    public ObjectListing getListObjectSummary();
}
