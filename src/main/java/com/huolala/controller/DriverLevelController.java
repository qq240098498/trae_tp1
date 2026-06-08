package com.huolala.controller;

import com.huolala.entity.DriverLevel;
import com.huolala.service.DriverLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/driverLevel")
public class DriverLevelController {
    @Autowired
    private DriverLevelService driverLevelService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("levels", driverLevelService.findAll());
        return "driverLevel/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("level", new DriverLevel());
        return "driverLevel/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        DriverLevel level = driverLevelService.findById(id);
        model.addAttribute("level", level);
        return "driverLevel/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute DriverLevel level) {
        driverLevelService.save(level);
        return "redirect:/driverLevel";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        driverLevelService.deleteById(id);
        return "redirect:/driverLevel";
    }
}
