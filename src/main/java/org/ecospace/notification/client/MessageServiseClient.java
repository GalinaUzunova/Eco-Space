package org.ecospace.notification.client;

import org.ecospace.notification.client.dto.ContactRequest;
import org.ecospace.notification.client.dto.ContactResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-svc", url = "http://localhost:8082/api/v1")
public interface MessageServiseClient {

@PostMapping("/contact")
    ResponseEntity<ContactResponse>submitContactForm(@RequestBody ContactRequest request);
}
