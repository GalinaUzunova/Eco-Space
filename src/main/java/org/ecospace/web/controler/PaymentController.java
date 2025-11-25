package org.ecospace.web.controler;

import org.ecospace.security.AuthenticationMetadata;
import org.ecospace.service.UserServiceImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class PaymentController {

    private final UserServiceImpl userService;

    public PaymentController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam Map<String, String> payFastData,
                                 RedirectAttributes redirectAttributes) {

        try {
            String merchantOrderId = payFastData.get("m_payment_id");
            boolean success = userService.completePayment(merchantOrderId, payFastData);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Payment completed successfully!");
                return "redirect:/successes";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Payment verification failed");
                return "redirect:fail";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:fail";
        }
    }

    @GetMapping("/cancel")
    public String paymentCancel(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                RedirectAttributes redirectAttributes) {

        try {
            userService.cancelPendingPayment(authenticationMetadata.getId());
            redirectAttributes.addFlashAttribute("infoMessage", "Payment was canceled");
            return "redirect:/client";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error canceling payment");
            return "redirect:fail";
        }
    }

    @PostMapping("/notify")
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String, String> payFastData) {
        try {
            String merchantOrderId = payFastData.get("m_payment_id");
            userService.completePayment(merchantOrderId, payFastData);
            return "OK"; // PayFast expects "OK" response
        } catch (Exception e) {
            return "ERROR";
        }
    }


}
