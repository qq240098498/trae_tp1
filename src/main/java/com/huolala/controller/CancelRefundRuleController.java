package com.huolala.controller;

import com.huolala.entity.CancelRefundRule;
import com.huolala.service.CancelRefundRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cancelRefundRule")
public class CancelRefundRuleController {
    @Autowired
    private CancelRefundRuleService cancelRefundRuleService;

    @GetMapping
    public String list(Model model) {
        List<CancelRefundRule> rules = cancelRefundRuleService.findAll();
        model.addAttribute("rules", rules);
        return "cancelRefundRule/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("rule", new CancelRefundRule());
        return "cancelRefundRule/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        CancelRefundRule rule = cancelRefundRuleService.findById(id);
        model.addAttribute("rule", rule);
        return "cancelRefundRule/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute CancelRefundRule rule) {
        cancelRefundRuleService.save(rule);
        return "redirect:/cancelRefundRule";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        cancelRefundRuleService.deleteById(id);
        return "redirect:/cancelRefundRule";
    }
}
