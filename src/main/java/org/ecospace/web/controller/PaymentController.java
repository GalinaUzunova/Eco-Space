package org.ecospace.web.controller;

import lombok.extern.slf4j.Slf4j;


import org.ecospace.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;



@Controller
@Slf4j
public class PaymentController {


    private final UserServiceImpl userService;

    @Autowired
    public PaymentController( UserServiceImpl userService) {

        this.userService = userService;
    }


    @GetMapping("/success")
    public String handlePaymentSuccess(@RequestParam String session_id,
                                       Model model) {
        log.info("Processing payment success for session: {}", session_id);

        userService.completePayment(session_id);

        model.addAttribute("successMessage",
                "✅ Payment successful! Your subscription has been activated.");
        return "success";
    }


    @GetMapping("/cancel")
    public String handlePaymentCancel(@RequestParam String order_id,
                                      Model model) {
        log.info("Payment cancelled for order: {}", order_id);

        model.addAttribute("infoMessage",
                "Payment was cancelled. You can try again anytime.");
        return "fail";
    }
}


