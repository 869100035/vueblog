package com.markerhub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class TestController {

    @GetMapping("/tt")
    public String test(){
        return  "ok";
    }
}
