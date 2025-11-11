package org.ecospace.web.controler;

import jakarta.validation.Valid;
import org.ecospace.notification.client.dto.ContactFormDto;
import org.ecospace.notification.service.MessageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class ContactController {

    public ContactController(MessageService messageService) {
        this.messageService = messageService;
    }

    @ModelAttribute("formDto")
    public ContactFormDto formDto(){
        return new ContactFormDto();
    }

    private  final MessageService messageService;
    @GetMapping("/contact")
    public String viewContactForm(Model model){
        model.addAttribute("currentPage", "contact");
        return "contact";

    }

    @PostMapping("/contact")

    public String handleForm(@Valid ContactFormDto formDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("formDto", formDto);
            redirectAttributes.addAttribute("org.springframework.validation.BindingResult.formFto", bindingResult);
        }
        boolean success = messageService.sendContactForm(formDto);

        if (success) {
            redirectAttributes.addFlashAttribute("success", true);
            return "redirect:/contact";

        } else {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("formDto", formDto);
            return "redirect:/contact";


        }

    }



    }

