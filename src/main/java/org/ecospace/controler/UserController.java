package org.ecospace.controler;


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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
public class UserController {


    private final UserServiceImpl userService;
    private final SubscriptionServiceImpl subscriptionService;

    private final ProductServiceImpl productService;


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
    private ProfileDto profileDto(){
        return  new ProfileDto();
    }



    public UserController(UserServiceImpl userService, SubscriptionServiceImpl subscriptionService, ProductServiceImpl productService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.productService = productService;
    }

    @GetMapping("/register")

    public String viewRegister() {

        return "register";
    }

    @PostMapping("/register")

    public String doRegister(@Valid UserDto userDto, BindingResult bindingResult, RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userDto", userDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userDto", bindingResult);

            return "redirect:/register";

        } else if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "Error", "Password don't match");

            return "register";

        } else if (!userService.userRegister(userDto)) {
            bindingResult.rejectValue("username", "Error", "Username already exist");
            return "redirect:/register";


        } else {
            userService.userRegister(userDto);
            return "login";
        }


    }


    @GetMapping("/login")

    public String viewLogin(@RequestParam(value = "error",required = false)String errorParam, Model model) {
      if(errorParam != null){
          model.addAttribute("errorMessage","Username or password is incorrect!");

      }
        return "login";
    }


    @GetMapping("/client")

    public String viewClient(@AuthenticationPrincipal AuthenticationMetadata authenticationPriciple , Model model){

        User user= userService.byId( authenticationPriciple.getId());

        List<Product> clientSubs = this.userService.getClentSubs(authenticationPriciple.getId());
        model.addAttribute("clientSubs", clientSubs);
        model.addAttribute("user", user);
        model.addAttribute("currentPage","client");



        if (!model.containsAttribute("subscriptionDto")) {
            model.addAttribute("subscriptionDto", new SubscriptionDtos());
        }

        return "client";
    }

    @PostMapping("/client")

    public String getClient(@Valid SubscriptionDtos subscriptionDto,BindingResult bindingResult
    ,RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("subscriptionDto", subscriptionDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.subscriptionDto",bindingResult);
            return "redirect:/client/";
        }
        UUID id=subscriptionDto.getId();
        return "redirect:/renew/"+ id;

    }



    @GetMapping("/payment/{id}")

    public String getPayment(@PathVariable("id") UUID id, Model model) {

        Subscription subscriptionUser = subscriptionService.byId(id);
        model.addAttribute("subscriptionUser", subscriptionUser);

        return "payment";
    }



    @PostMapping("/payment/{id}")

   public String doPayment(@PathVariable("id") UUID id, @Valid UserCardDto cardDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, @AuthenticationPrincipal AuthenticationMetadata authenticationPriciple) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("cardDto", cardDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.cardDto", bindingResult);
            return "redirect:/payment/" + id;
        }

        this.userService.buyProduct(authenticationPriciple, cardDto, id);
        return "successes";
    }

    @PostMapping("/renew/{id}")

    private String renewProduct(@PathVariable("id") UUID id, @Valid UserCardDto cardDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, @AuthenticationPrincipal AuthenticationMetadata authenticationPriciple) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("cardDto", cardDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.cardDto", bindingResult);
            return "redirect:/renew/" + id;
        }

        this.userService.renew(authenticationPriciple, cardDto, id);
        return "successes";

    }

    @GetMapping("/renew/{id}")

    private String getRenewPage(@PathVariable UUID id,Model model){

        Product product=productService.findById(id);
        model.addAttribute("product",product);



        return "renew";

    }
    @GetMapping("/edit-profile/update/{id}")

    private String viewProfile(@PathVariable("id") UUID id,Model model){
      User user=this.userService.byId(id);
        ProfileDto editProfile= ProfileDtoMapper.fromUser(user);
        model.addAttribute("user", user);
        model.addAttribute("editProfile", editProfile);

        return "edit-profile";
    }

   @PutMapping ("/edit-profile/update/{id}")
    public String editProfile(@PathVariable("id") UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,@Valid ProfileDto editProfile,BindingResult bindingResult, RedirectAttributes redirectAttributes)  {

         if(bindingResult.hasErrors()){
             User user=userService.byId(id);
             redirectAttributes.addFlashAttribute("user", user);
             redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", user);
             System.out.println("Password nol log");
             return "redirect:/edit-profile";
         }
         this.userService.editProfile(editProfile,id,authenticationMetadata);
         String role=authenticationMetadata.getRole().toString();
         if(role.equals("ADMIN")) {
           return "redirect:/manager";
       }
       return "redirect:/client";
    }

}






