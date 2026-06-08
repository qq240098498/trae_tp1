package com.huolala.controller;

import com.huolala.entity.BillingRuleItem;
import com.huolala.service.BillingRuleItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/billingRule")
public class BillingRuleItemController {
    @Autowired
    private BillingRuleItemService billingRuleItemService;

    @GetMapping
    public String list(Model model) {
        List<BillingRuleItem> rules = billingRuleItemService.findAll();
        model.addAttribute("rules", rules);
        return "billingRule/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("rule", new BillingRuleItem());
        return "billingRule/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        BillingRuleItem rule = billingRuleItemService.findById(id);
        model.addAttribute("rule", rule);
        return "billingRule/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute BillingRuleItem rule) {
        billingRuleItemService.save(rule);
        return "redirect:/billingRule";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        BillingRuleItem toggled = billingRuleItemService.toggleEnabled(id);
        if (toggled != null) {
            redirectAttributes.addFlashAttribute("message",
                    toggled.getRuleName() + " 已" + (toggled.getEnabled() == 1 ? "启用" : "禁用"));
        }
        return "redirect:/billingRule";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        billingRuleItemService.deleteById(id);
        return "redirect:/billingRule";
    }
}
