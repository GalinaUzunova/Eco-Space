package org.ecospace.controler;

import org.ecospace.model.dto.DesignSubscriptionDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SubscriptionController {
    @GetMapping("/design")
        public String viewDesign(){
        return "design";
        }

        @PostMapping("/design")

        public String designPackiges(DesignSubscriptionDto designDto){

        return "/design";


        }

        @GetMapping("/maintenance")
    public  String viewMaintanacep(){

        return "maintenance";
        }
}
