package org.ecospace.web.controler;

import jakarta.validation.Valid;

import org.ecospace.notification.client.MessageServiceClient;
import org.ecospace.notification.client.dto.ContactFormDto;
import org.ecospace.notification.client.dto.ContactRequest;
import org.ecospace.notification.service.MessageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
public class ContactController {

    private final MessageService messageService;
    private final MessageServiceClient client;

    public ContactController(MessageService messageService, MessageServiceClient client) {
        this.messageService = messageService;
        this.client = client;
    }

    @ModelAttribute("formDto")
    public ContactFormDto formDto() {
        return new ContactFormDto();
    }


    @GetMapping("/contact")
    public String viewContactForm(Model model) {
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

    @GetMapping("/contact-message")
    public String viewPage( Model model) {
        List<ContactRequest> allMessages = client.viewMessages();
        List<ContactRequest> todayMessages = client.viewTodaySentMessages();

        model.addAttribute("allMessages", allMessages);
        model.addAttribute("todayMessages", todayMessages);
        return "contact-message";


    }
    @GetMapping("/contact-message/delete/{id}")
    public String deleteMessage(@PathVariable("id")UUID id){
        client.deleteMessage(id);
        return "redirect:/contact-message";

    }


}

