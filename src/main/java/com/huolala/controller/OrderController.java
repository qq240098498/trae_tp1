package com.huolala.controller;

import com.huolala.entity.Order;
import com.huolala.service.DriverService;
import com.huolala.service.FreightConfigService;
import com.huolala.service.OrderService;
import com.huolala.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private DriverService driverService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private FreightConfigService freightConfigService;

    @GetMapping
    public String list(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "order/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("freightConfigs", freightConfigService.findActive());
        return "order/form";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("drivers", driverService.findActive());
        model.addAttribute("vehicles", vehicleService.findActive());
        return "order/detail";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Order order) {
        orderService.createOrder(order);
        return "redirect:/order";
    }

    @PostMapping("/accept/{id}")
    public String accept(@PathVariable Long id, @RequestParam Long driverId, @RequestParam Long vehicleId,
                         RedirectAttributes redirectAttributes) {
        try {
            orderService.acceptOrder(id, driverId, vehicleId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/detail/" + id;
    }

    @PostMapping("/arrive/{id}")
    public String arrive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.arriveOrder(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/detail/" + id;
    }

    @PostMapping("/complete/{id}")
    public String complete(@PathVariable Long id, @RequestParam(required = false) Integer waitMinutes,
                           RedirectAttributes redirectAttributes) {
        try {
            orderService.completeOrder(id, waitMinutes);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/detail/" + id;
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/detail/" + id;
    }
}
