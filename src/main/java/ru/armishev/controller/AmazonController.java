package ru.armishev.controller;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.entity.AmazonObjectOwnerEntity;
import ru.armishev.jpa.AmazonObjectJPA;
import ru.armishev.service.AmazonService;

import java.util.*;

@RestController
@RequestMapping(value="/api/v1/list")
public class AmazonController {
    private final AmazonObjectJPA amazonObjectJPA;
    private final AmazonService amazonService;

    @Autowired
    public AmazonController(AmazonObjectJPA amazonObjectJPA, AmazonService amazonService) {
        this.amazonObjectJPA = amazonObjectJPA;
        this.amazonService = amazonService;
    }

    @GetMapping("")
    public List<AmazonObjectEntity> getList() {
        return amazonObjectJPA.findAll();
    }

    @GetMapping("/add")
    public List<AmazonObjectEntity> addElement() {
        //ObjectListing list = amazonService.list();
        List<AmazonObjectEntity> listForSave = new ArrayList<>();

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

        listForSave.add(newAmazonObjectEntity);

        amazonObjectJPA.save(newAmazonObjectEntity);

        /*for (S3ObjectSummary objectSummary : list.getObjectSummaries()) {
            AmazonObjectEntity newAmazonObjectEntity = AmazonObjectEntity.convertFromAmazon(objectSummary);

            listForSave.add(newAmazonObjectEntity);

            amazonObjectJPA.save(newAmazonObjectEntity);
        }*/

        if (!listForSave.isEmpty()) {
            amazonObjectJPA.saveAll(listForSave);
        }

        return listForSave;
    }
}
