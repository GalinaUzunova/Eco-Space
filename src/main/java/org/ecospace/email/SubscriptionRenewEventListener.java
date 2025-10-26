package org.ecospace.email;

import org.ecospace.event.SubscriptionRenewEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class SubscriptionRenewEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionRenewEventListener.class);

    private final EmailService emailService;

    public SubscriptionRenewEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    @Async
    public void handleSubscriptionRenewal(SubscriptionRenewEvent event) {
        try {
            logger.info("Processing subscription renewal event for user: {}", event.getUsername());
            sendEmailNotification(event);
            logger.info("Successfully sent renewal email to: {}", event.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send renewal notification to: {}", event.getEmail(), e);

        }
    }

    private void sendEmailNotification(SubscriptionRenewEvent event) {
        String subject = "Subscription Renewal Reminder";
        String body = buildEmailBody(event);
        emailService.sendEmail(event.getEmail(), subject, body);
    }

    private String buildEmailBody(SubscriptionRenewEvent event) {
        return String.format("""
            Dear %s,
            
            Your subscription "%s" will expire on %s.
            Please renew your subscription to continue enjoying our services.
            
            Subscription Price: $%.2f
            
            Best regards,
            Eco-Space Team
            """,
                event.getUsername(),
                event.getSubscriptionName(),
                event.getExpiredOn().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm")),
                event.getPrice()
        );
    }
}