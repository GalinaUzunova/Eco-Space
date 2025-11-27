package org.ecospace.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    @GetMapping("/home")

    public String aboutPage(Model model) {
        model.addAttribute("currentPage", "home");

        return "home";
    }

}
