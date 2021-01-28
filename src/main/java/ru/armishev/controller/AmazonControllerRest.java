package ru.armishev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.jpa.AmazonObjectJPA;

import javax.validation.constraints.Min;
import java.util.*;

@RestController
@RequestMapping(value="/api/v1/list")
public class AmazonControllerRest {
    private final AmazonObjectJPA amazonObjectJPA;
    private static final int COUNT_OBJ_ON_PAGE = 10;

    @Autowired
    public AmazonControllerRest(AmazonObjectJPA amazonObjectJPA) {
        this.amazonObjectJPA = amazonObjectJPA;
    }

    @GetMapping("")
    public List<AmazonObjectEntity> getList(Model model, @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int currentPage) {
        Pageable firstPageWithTwoElements = PageRequest.of(currentPage, COUNT_OBJ_ON_PAGE);
        Page<AmazonObjectEntity> page = amazonObjectJPA.findAll(firstPageWithTwoElements);

        return page.getContent();
    }
}
