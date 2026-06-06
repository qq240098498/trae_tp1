package com.huolala.controller;

import com.huolala.entity.Vehicle;
import com.huolala.service.DriverService;
import com.huolala.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/vehicle")
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private DriverService driverService;

    @GetMapping
    public String list(Model model) {
        List<Vehicle> vehicles = vehicleService.findAll();
        model.addAttribute("vehicles", vehicles);
        return "vehicle/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("drivers", driverService.findActive());
        return "vehicle/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleService.findById(id);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("drivers", driverService.findActive());
        return "vehicle/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Vehicle vehicle) {
        vehicleService.save(vehicle);
        return "redirect:/vehicle";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        vehicleService.deleteById(id);
        return "redirect:/vehicle";
    }
}
