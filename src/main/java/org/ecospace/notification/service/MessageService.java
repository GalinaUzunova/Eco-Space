package org.ecospace.notification.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.ecospace.notification.client.MessageServiseClient;
import org.ecospace.notification.client.dto.ContactFormDto;
import org.ecospace.notification.client.dto.ContactRequest;
import org.ecospace.notification.client.dto.ContactResponse;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class MessageService {

    private final MessageServiseClient messageServiseClient;

    public MessageService(MessageServiseClient messageServiseClient) {
        this.messageServiseClient = messageServiseClient;
    }

    public boolean sendContactForm(ContactFormDto contactFormDto) {

        try {

            ContactRequest contactRequest = ContactRequest.builder()
                    .name(contactFormDto.getName())
                    .email(contactFormDto.getEmail())
                    .phone(contactFormDto.getPhone())
                    .message(contactFormDto.getMessage())
                    .subject("Contact-Form")
                    .build();
          log.info("🔄 Calling Message Microservice...");
            ContactResponse response = messageServiseClient.submitContactForm(contactRequest).getBody();


            log.info("✅ Message processed successfully! ID: " + response.getId());
            return true;


        } catch (FeignException.FeignClientException e) {
           log.error("[S2S Call]: Failed due to %s.".formatted(e.getMessage()));
            return false;

        }


    }
}

