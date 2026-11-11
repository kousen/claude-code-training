package com.kousen.cert.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Simple controller to handle the root path
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/index.html");
    }
}