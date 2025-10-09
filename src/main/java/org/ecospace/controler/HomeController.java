package org.ecospace.controler;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String viewHome(){
    return "home";

    }
    @GetMapping("/contact")

    public String viewContact(){
        return "contact";
    }

    @GetMapping("/subscriptions")

    public String viewSubs(){

        return "subscriptions";
    }
}
