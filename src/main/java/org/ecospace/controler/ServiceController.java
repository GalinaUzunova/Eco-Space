package org.ecospace.controler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServiceController {
    @GetMapping("/design")
        public String viewdesign(){
        return "design";
        }

        @GetMapping("/maintenance")
    public  String viewMaintanacep(){

        return "maintenance";
        }
}
