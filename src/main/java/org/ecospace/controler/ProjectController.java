package org.ecospace.controler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectController {
    @GetMapping("/projects")
    public String viewProject(){
        return "projects";
    }

    @GetMapping("/add-project")
    public String viewAdd(){

        return "add-project";
    }
}
