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
public class AmazonControllerRest {
    private final AmazonObjectJPA amazonObjectJPA;

    @Autowired
    public AmazonControllerRest(AmazonObjectJPA amazonObjectJPA) {
        this.amazonObjectJPA = amazonObjectJPA;
    }

    @GetMapping("")
    public List<AmazonObjectEntity> getList() {
        return amazonObjectJPA.findAll();
    }
}
