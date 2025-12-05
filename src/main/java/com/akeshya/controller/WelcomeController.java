package com.akeshya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class WelcomeController {



    @GetMapping("/index")
    public String indexPage() {
        return "index.html	";  // same template page, different URL
    }
}
