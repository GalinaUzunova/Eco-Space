package org.ecospace.controler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    @GetMapping("/register")

    public String viewRegister(){

        return "register";
    }

    @GetMapping("/login")

    public String viewLogin() {

        return "login";
    }

   @GetMapping("/client")

    public String viewClient(){
        return "client";
   }
}
