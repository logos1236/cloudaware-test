package ru.armishev.service;

import ru.armishev.entity.AmazonObjectEntity;
import java.util.List;

public interface IAmazonService {
    public List<AmazonObjectEntity> getListAmazonObjectEntity();
    public boolean isLoopEnd();
}
