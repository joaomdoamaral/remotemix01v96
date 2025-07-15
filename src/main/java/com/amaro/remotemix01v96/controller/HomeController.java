package com.amaro.remotemix01v96.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String redirectToIndex() {
        return "forward:/index.html";
    }
}
