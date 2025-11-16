package org.ecospace.notification.client;

import org.ecospace.notification.client.dto.ContactRequest;
import org.ecospace.notification.client.dto.ContactResponse;

import org.ecospace.notification.client.dto.SubscriptionRemainderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notification-svc", url = "http://localhost:8082/api/v1")
public interface MessageServiceClient {

@PostMapping("/contact")
    ResponseEntity<ContactResponse>submitContactForm(@RequestBody ContactRequest request);

    @PostMapping("/sms/subscription-reminders")
    void sendSubscriptionReminder(@RequestBody SubscriptionRemainderRequest request);

    @GetMapping("/contact/contact-messages")
   List<ContactRequest>viewMessages();

    @GetMapping("/contact/contact-messages/today")
    List<ContactRequest>viewTodaySentMessages();


    @DeleteMapping("/contact/contact-messages/delete/{id}")
    ResponseEntity<String> deleteMessage(@PathVariable UUID id);
}

