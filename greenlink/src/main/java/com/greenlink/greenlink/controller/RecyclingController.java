package com.greenlink.greenlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reciclare")
public class RecyclingController {

    @GetMapping
    public String showRecyclingMap() {
        return "reciclare";
    }
}
