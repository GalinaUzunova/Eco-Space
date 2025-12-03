package org.ecospace.web.controller;

import lombok.extern.slf4j.Slf4j;


import org.ecospace.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;


@Controller
@Slf4j
public class PaymentController {

    private final UserServiceImpl userService;

    @Autowired
    public PaymentController(UserServiceImpl userService) {

        this.userService = userService;
    }

    @GetMapping("/success")
    public String handlePaymentSuccess(@RequestParam String session_id) {

        boolean isCompleted = userService.completePayment(session_id);

        if (isCompleted) {
            return "success";

        }
        return "redirect:/fail?session_id=" + session_id;


    }

    @GetMapping("/cancel")
    public String handlePaymentCancel(@RequestParam String session_id) {

        userService.clearPendingPaymentBySession(session_id);

        return "fail";
    }

    @GetMapping("/fail")
    public String handlePaymentFail(@RequestParam String session_id,
                                    @RequestParam(required = false) String reason) {

        userService.clearPendingPaymentBySession(session_id);
        log.warn("Payment failed for session: {}, reason: {}", session_id, reason);
        return "fail";
    }
}


