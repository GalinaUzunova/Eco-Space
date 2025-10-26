package org.ecospace.controler;





import org.springframework.stereotype.Controller;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {




    @GetMapping("/contact")

    public String viewContact(Model model){

        model.addAttribute("currentPage","contact");
        return "contact";
    }


    @GetMapping("/home")

    public String aboutPage(Model model){
        model.addAttribute("currentPage","home");


        return "home";
    }

}
