package org.ecospace.controler;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.ecospace.model.Subscription;
import org.ecospace.model.User;
import org.ecospace.model.dto.LoginDto;
import org.ecospace.model.dto.SubscriptionDtos;
import org.ecospace.model.dto.UserCardDto;
import org.ecospace.model.dto.UserDto;
import org.ecospace.service.SubscriptionServiceImpl;
import org.ecospace.service.UserServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
public class UserController {


    private final UserServiceImpl userService;
    private final SubscriptionServiceImpl subscriptionService;


    @ModelAttribute("subscriptionDto")
    private SubscriptionDtos get() {
        return new SubscriptionDtos();
    }



    @ModelAttribute("userDto")
    private UserDto create() {
        return new UserDto();
    }

    @ModelAttribute("loginDto")
    private LoginDto createLogin() {
        return new LoginDto();
    }

    @ModelAttribute("cardDto")
    private UserCardDto cardDto() {
        return new UserCardDto();
    }


    public UserController(UserServiceImpl userService, SubscriptionServiceImpl subscriptionService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;

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

    @PostMapping("/login")

    public String doLogin(@Valid LoginDto loginDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpSession httpSession) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("loginDto", loginDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.loginDto", bindingResult);

            return "redirect:/login";
        }

        User user = userService.login(loginDto);
        httpSession.setAttribute("userId", user.getId());

        return "redirect:/client";


    }

    @GetMapping("/login")

    public String viewLogin() {

        return "login";
    }


    @GetMapping("/client")

    public String viewClient(Model model, HttpSession session) {

        UUID id = (UUID) session.getAttribute("userId");
        User user = userService.byId(id);

        List<Subscription> clientSubs = this.userService.getClentSubs(id);
        model.addAttribute("clientSubs", clientSubs);
        model.addAttribute("user", user);

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
        return "redirect:/payment/"+ subscriptionDto.getId();

    }

    @GetMapping("/logout")

    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";

    }

    @GetMapping("/payment/{id}")

    private String getPayment(@PathVariable("id") UUID id, Model model) {

        Subscription subscriptionUser = subscriptionService.byId(id);
        model.addAttribute("subscriptionUser", subscriptionUser);

        return "payment";
    }



    @PostMapping("/payment/{id}")

    private String doPayment(@PathVariable("id") UUID id, @Valid UserCardDto cardDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpSession httpSession) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("cardDto", cardDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.cardDto", bindingResult);
            return "redirect:/payment/" + id;
        }

        this.userService.buySubscription(httpSession, cardDto, id);
        return "redirect:/client";
    }
}






