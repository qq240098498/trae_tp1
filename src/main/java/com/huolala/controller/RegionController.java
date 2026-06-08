package com.huolala.controller;

import com.huolala.entity.Region;
import com.huolala.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/region")
public class RegionController {
    @Autowired
    private RegionService regionService;

    @GetMapping
    public String list(Model model) {
        List<Region> regions = regionService.findAll();
        model.addAttribute("regions", regions);
        return "region/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("region", new Region());
        model.addAttribute("parentRegions", regionService.findByLevel(1));
        return "region/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Region region = regionService.findById(id);
        model.addAttribute("region", region);
        model.addAttribute("parentRegions", regionService.findByLevel(1));
        return "region/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Region region) {
        regionService.save(region);
        return "redirect:/region";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        regionService.deleteById(id);
        return "redirect:/region";
    }
}
