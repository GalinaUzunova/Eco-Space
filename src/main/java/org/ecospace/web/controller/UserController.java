package org.ecospace.web.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.ecospace.model.Product;
import org.ecospace.model.Subscription;
import org.ecospace.model.User;
import org.ecospace.model.dto.*;
import org.ecospace.security.AuthenticationMetadata;
import org.ecospace.service.ProductServiceImpl;
import org.ecospace.service.SubscriptionServiceImpl;
import org.ecospace.service.UserServiceImpl;
import org.ecospace.utility.CancelSubsUtill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.UUID;


@Slf4j
@Controller
public class UserController {

    private final SubscriptionServiceImpl subscriptionService;
    private final ProductServiceImpl productService;

    private final UserServiceImpl userServiceImpl;


    private final CancelSubsUtill cancelSubsUtill;


    @ModelAttribute("subscriptionDto")
    private SubscriptionDtos get() {
        return new SubscriptionDtos();
    }

    @ModelAttribute("userDto")
    private UserDto create() {
        return new UserDto();
    }

    @ModelAttribute("cardDto")
    private UserCardDto cardDto() {
        return new UserCardDto();
    }

    @ModelAttribute("editProfile")
    private ProfileDto profileDto() {
        return new ProfileDto();
    }

    @Autowired
    public UserController(SubscriptionServiceImpl subscriptionService, ProductServiceImpl productService, UserServiceImpl userServiceImpl, CancelSubsUtill cancelSubsUtill) {

        this.subscriptionService = subscriptionService;
        this.productService = productService;
        this.userServiceImpl = userServiceImpl;


        this.cancelSubsUtill = cancelSubsUtill;
    }

    @GetMapping("/register")
    public String viewRegister() {
        return "register";
    }

    @PostMapping("/register")

    public String doRegister(@Valid @ModelAttribute("userDto") UserDto userDto,
                             BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (userServiceImpl.userExists(userDto)) {
            bindingResult.rejectValue("username", "error.userDto", "Username already exists");
            return "register";
        }


        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.userDto", "Passwords do not match");
            return "register";
        }


        userServiceImpl.userRegister(userDto);
        return "redirect:/login?success";
    }


    @GetMapping("/login")
    public String viewLogin(@RequestParam(value = "error", required = false) String errorParam, Model model) {
        if (errorParam != null) {
            model.addAttribute("errorMessage", "Username or password is incorrect!");
        }
        return "login";
    }

    @GetMapping("/client")
    public String viewClient(@AuthenticationPrincipal AuthenticationMetadata authenticationPrinciple, Model model) {
        User user = userServiceImpl.byId(authenticationPrinciple.getId());
        List<Product> clientSubs = this.userServiceImpl.getClientSubs(authenticationPrinciple.getId());
        model.addAttribute("clientSubs", clientSubs);
        model.addAttribute("user", user);
        model.addAttribute("currentPage", "client");
        if (!model.containsAttribute("subscriptionDto")) {
            model.addAttribute("subscriptionDto", new SubscriptionDtos());
        }
        return "client";
    }

    @PostMapping("/client")
    public String getClient(@Valid SubscriptionDtos subscriptionDto, BindingResult bindingResult
            , RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("subscriptionDto", subscriptionDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.subscriptionDto", bindingResult);
            return "redirect:/client/";
        }
        UUID id = subscriptionDto.getId();
        return "redirect:/renew/" + id;

    }

    @PostMapping("/client/subscription/cancel")
    public String cancelSubscription(@RequestParam UUID subscriptionId, @AuthenticationPrincipal AuthenticationMetadata user, RedirectAttributes redirectAttributes) {

        this.cancelSubsUtill.cancelSubscription(user, subscriptionId, redirectAttributes);
        return "redirect:/client";

    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/payment/{id}")
    public String showPaymentPage(@PathVariable("id") UUID id, Model model) {
        Subscription subscriptionUser = subscriptionService.byId(id);
        model.addAttribute("subscriptionUser", subscriptionUser);

        return "payment";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("payment/initiate/{id}")
    public String initiatePayment(@PathVariable("id") UUID id,
                                  @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                  RedirectAttributes redirectAttributes) {
        log.info("Initiating payment for subscription: {}, user: {}", id, authenticationMetadata.getId());

        String stripeCheckoutUrl = userServiceImpl.initiatePayment(authenticationMetadata, id);

        log.info("Redirecting to Stripe checkout");
        return "redirect:" + stripeCheckoutUrl;
    }


    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/renew/{id}")
    public String renewProduct(@PathVariable("id") UUID id, @Valid UserCardDto cardDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, @AuthenticationPrincipal AuthenticationMetadata metadata) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("cardDto", cardDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.cardDto", bindingResult);
            return "redirect:/renew/" + id;
        }
        userServiceImpl.renew(metadata, cardDto, id);
        return "success";

    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/renew/{id}")
    public String getRenewPage(@PathVariable UUID id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "renew";
    }

    @GetMapping("/edit-profile/update/{id}")
    public String viewProfile(@PathVariable("id") UUID id, Model model) {
        User user = this.userServiceImpl.byId(id);
        ProfileDto editProfile = ProfileDtoMapper.fromUser(user);
        model.addAttribute("user", user);
        model.addAttribute("editProfile", editProfile);

        return "edit-profile";
    }

    @PutMapping("/edit-profile/update/{id}")
    public String editProfile(@PathVariable("id") UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, @Valid ProfileDto editProfile, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {

            User user = userServiceImpl.byId(id);
            model.addAttribute("user", user);

            return "redirect:/edit-profile";
        }
        this.userServiceImpl.editProfile(editProfile, authenticationMetadata);
        String role = authenticationMetadata.getRole().toString();
        if (role.equals("ADMIN")) {
            return "redirect:/manager";
        }
        return "redirect:/client";
    }

}







