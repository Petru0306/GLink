package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.RecyclingPointDto;
import com.greenlink.greenlink.service.RecyclingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/reciclare")
public class RecyclingController {

    @Autowired
    private RecyclingService recyclingService;

    @GetMapping
    public String getAllRecyclingPoints(Model model) {
        List<RecyclingPointDto> points = recyclingService.getAllRecyclingPoints();
        model.addAttribute("recyclingPoints", points);
        return "reciclare"; // Returnează fișierul recycling.html
    }

    @GetMapping("/point/{pointId}")
    public String getRecyclingPointDetails(@PathVariable Long pointId, Model model) {
        RecyclingPointDto point = recyclingService.getRecyclingPointById(pointId);
        model.addAttribute("recyclingPoint", point);
        return "recycling-point-details"; // Returnează fișierul recycling-point-details.html
    }
}
