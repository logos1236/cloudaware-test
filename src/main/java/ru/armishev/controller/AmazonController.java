package ru.armishev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.jpa.AmazonObjectJPA;

import javax.validation.constraints.Min;
import java.util.List;

@Controller
@RequestMapping(value="/list")
public class AmazonController {
    private final AmazonObjectJPA amazonObjectJPA;
    private static final int countObjOnPage = 10;

    @Autowired
    public AmazonController(AmazonObjectJPA amazonObjectJPA) {
        this.amazonObjectJPA = amazonObjectJPA;
    }

    @GetMapping("")
    public String getList(Model model, @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int currentPage) {
        Pageable firstPageWithTwoElements = PageRequest.of(currentPage, countObjOnPage);
        Page<AmazonObjectEntity> page = amazonObjectJPA.findAll(firstPageWithTwoElements);

        model.addAttribute("listS3", page.getContent());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalCountElements", page.getTotalElements());

        return "list";
    }
}
