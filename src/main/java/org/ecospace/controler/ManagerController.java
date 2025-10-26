package org.ecospace.controler;


import org.ecospace.model.User;

import org.ecospace.security.AuthenticationMetadata;
import org.ecospace.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ManagerController {


   private final UserServiceImpl userService;
  @Autowired
    public ManagerController(UserServiceImpl userService) {
        this.userService = userService;
    }


   @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/manager")

    public String manager() {



        return "manager";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/subscriptions")

    public String viewSubs(Model model){

       List<User> allUsers= userService.getAllUsersAndSubs();
        model.addAttribute("allUsers", allUsers);

        return "subscriptions";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit-client")

    public String editClient(Model model){

      List<User>clients=userService.getAllUsers();
      model.addAttribute("clients", clients);

        return "edit-client";
    }

}
