package org.ecospace.controler;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.ecospace.model.SubscriptionType;
import org.ecospace.model.dto.LoginDto;
import org.ecospace.model.dto.UserDto;
import org.ecospace.service.UserServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class UserController {


    private final UserServiceImpl userService;


    @ModelAttribute("userDto")
    private UserDto create() {
        return new UserDto();
    }
    @ModelAttribute("loginDto")
    private LoginDto createLogin(){
        return new LoginDto();
    }
    @ModelAttribute("subType")
    public SubscriptionType[] subscriptionType(){
        return SubscriptionType.values();
    }

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
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
            bindingResult.rejectValue("username", "Error", "Username is already occupied");
            return "redirect:/register";



        } else {

            return "login";
        }


    }

    @PostMapping("/login")

    public String doLogin(@Valid LoginDto loginDto,BindingResult bindingResult, RedirectAttributes redirectAttributes ){

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("loginDto",loginDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.loginDto", bindingResult);

            return "redirect:/login";
        }
       boolean isLoged= userService.login(loginDto);
        if(!isLoged){
            return "redirect:/login";
        }

        return "home";


    }

    @GetMapping("/login")

    public String viewLogin() {

        return "login";
    }





    @GetMapping("/client")

    public String viewClient() {
        return "client";
    }


}
