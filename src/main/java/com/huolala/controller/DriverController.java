package com.huolala.controller;

import com.huolala.entity.Driver;
import com.huolala.service.DriverLevelService;
import com.huolala.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/driver")
public class DriverController {
    @Autowired
    private DriverService driverService;

    @Autowired
    private DriverLevelService driverLevelService;

    @GetMapping
    public String list(Model model) {
        List<Driver> drivers = driverService.findAll();
        model.addAttribute("drivers", drivers);
        model.addAttribute("levels", driverLevelService.findAll());
        return "driver/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("driver", new Driver());
        model.addAttribute("levels", driverLevelService.findAll());
        return "driver/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Driver driver = driverService.findById(id);
        model.addAttribute("driver", driver);
        model.addAttribute("levels", driverLevelService.findAll());
        return "driver/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Driver driver) {
        driverService.save(driver);
        return "redirect:/driver";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        driverService.deleteById(id);
        return "redirect:/driver";
    }
}
