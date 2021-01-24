package ru.armishev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.jpa.AmazonObjectJPA;
import ru.armishev.service.IAmazonEntity;

import java.util.*;

@RestController
@RequestMapping(value="/api/v1/list")
public class AmazonController {
    private final AmazonObjectJPA amazonObjectJPA;
    private final IAmazonEntity amazonEntity;

    @Autowired
    public AmazonController(AmazonObjectJPA amazonObjectJPA, IAmazonEntity amazonService) {
        this.amazonObjectJPA = amazonObjectJPA;
        this.amazonEntity = amazonService;
    }

    @GetMapping("")
    public List<AmazonObjectEntity> getList() {
        return amazonObjectJPA.findAll();
    }

    @GetMapping("/add")
    public List<AmazonObjectEntity> addElement() {
        List<AmazonObjectEntity> listForSave = amazonEntity.getList();

        if (!listForSave.isEmpty()) {
            amazonObjectJPA.saveAll(listForSave);
        }

        return listForSave;
    }
}
