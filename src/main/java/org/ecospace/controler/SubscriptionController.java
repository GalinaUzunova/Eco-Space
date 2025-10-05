package org.ecospace.controler;

import jakarta.validation.Valid;
import org.ecospace.model.Subscription;
import org.ecospace.model.SubscriptionType;
import org.ecospace.model.dto.*;
import org.ecospace.service.SubscriptionServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller

public class SubscriptionController {

    private final SubscriptionServiceImpl subscriptionService;
    public SubscriptionController(SubscriptionServiceImpl subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    @ModelAttribute("editedDto")
    public EditSubDto editedGet(){

        return new EditSubDto();
    }
    @ModelAttribute("subDto")
    public AddSubDto dto() {
        return new AddSubDto();
    }
    @ModelAttribute("subType")
    public SubscriptionType []subType(){
        return SubscriptionType.values();
    }



    @GetMapping("/design")
    public String viewDesign(Model model) {

      List<DesignSubscriptionDto>byDesign=  subscriptionService.getDesignSubscriptions();
      model.addAttribute("byDesign", byDesign);

        return "design";
    }


    @PostMapping("/design")

    public String designPackiges(DesignSubscriptionDto designDto) {

        return "/design";

    }


    @GetMapping("/add-subscription")

    public String viewpage() {
        return "add-subscription";
    }



    @PostMapping("/add-subscription")

    public String createSub(@Valid AddSubDto subDto, BindingResult bindingResult, RedirectAttributes
            redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("subDto", subDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.subDto",
                    bindingResult);
            return "redirect:/add-subscription";
        }
        subscriptionService.addNewSubscription(subDto);
        if (subDto.getType().name().equals("DESIGN")) {
            return "redirect:/design";
        }
        return "redirect:/maintenance";

    }


    @GetMapping("/maintenance")
    public String viewMaintenance(Model model) {
     List<MaintenanceSubDto> maintenanceSubscriptions= subscriptionService.getMaintenanceSubscriptions();
      model.addAttribute("maintenanceSubscriptions",maintenanceSubscriptions);

        return "maintenance";
    }


    @GetMapping("/edit-subscription/{id}")
            public String viewEditForm(@PathVariable("id") UUID id,Model model) {


        Subscription subscription=subscriptionService.byId(id);
        EditSubDto editedDto=DtoMapper.fromSubscription(subscription);
        model.addAttribute("editedDto", editedDto);
        model.addAttribute("subscription", subscription);


        return "/edit-subscription";
    }

    @PutMapping ("/edit-subscription/{id}")

    public String viewEditSub(@PathVariable("id") UUID id,@Valid EditSubDto editedDto,BindingResult bindingResult,
                              RedirectAttributes redirectAttributes){

     if(bindingResult.hasErrors()){
       Subscription subscription=subscriptionService.byId(id);
       redirectAttributes.addFlashAttribute("subscription", subscription);
       redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.subscription",subscription);

       return "/edit-subscription";
     }

        this.subscriptionService.editSubscription(id, editedDto);
         if(editedDto.getType().name().equals("DESIGN")){
             return "redirect:/design";
         }
        return "redirect:/maintenance";
    }

    @GetMapping("edit-subscription/remove/{id}")

    public String removePackage(@PathVariable("id") UUID id ){
         Subscription subscription=subscriptionService.byId(id);
         subscriptionService.delete(id);
         if(subscription.getType().toString().equals("DESIGN")){
             return "redirect:/design";
         }
        return "redirect:/maintenance";
    }

}
