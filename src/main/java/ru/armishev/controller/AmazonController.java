package ru.armishev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.jpa.AmazonObjectJPA;

import java.util.List;

@Controller
@RequestMapping(value="/list")
public class AmazonController {
    private final AmazonObjectJPA amazonObjectJPA;

    @Autowired
    public AmazonController(AmazonObjectJPA amazonObjectJPA) {
        this.amazonObjectJPA = amazonObjectJPA;
    }

    @GetMapping("")
    public String getList(Model model) {
        model.addAttribute("listS3", amazonObjectJPA.findAll());

        return "list";
    }
}
