package com.tictactoe.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @RequestMapping("/")
    public String greet() {
        return "Welcome to Telusko";
    }

    @RequestMapping("/about")
    public String about() {
        return "This is GameService";
    }
}
