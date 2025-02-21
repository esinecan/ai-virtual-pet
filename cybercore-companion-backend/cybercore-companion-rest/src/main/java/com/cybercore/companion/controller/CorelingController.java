package com.cybercore.companion.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CorelingController {

    @GetMapping("/status")
    public String getStatus() {
        return "REST API is running";
    }
}
