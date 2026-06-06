package com.huolala.controller;

import com.huolala.entity.FreightConfig;
import com.huolala.service.FreightConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/freight")
public class FreightController {
    @Autowired
    private FreightConfigService freightConfigService;

    @GetMapping
    public String list(Model model) {
        List<FreightConfig> configs = freightConfigService.findAll();
        model.addAttribute("configs", configs);
        return "freight/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("config", new FreightConfig());
        return "freight/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        FreightConfig config = freightConfigService.findById(id);
        model.addAttribute("config", config);
        return "freight/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute FreightConfig config) {
        freightConfigService.save(config);
        return "redirect:/freight";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        freightConfigService.deleteById(id);
        return "redirect:/freight";
    }
}
