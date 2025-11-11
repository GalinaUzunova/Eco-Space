package org.ecospace.notification.service;

import org.ecospace.event.SubscriptionRenewEvent;
import org.springframework.context.event.EventListener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Async
    @EventListener

    public  void  sendNotification(SubscriptionRenewEvent event){

        System.out.printf("Send it to user %s with renewal Date %s",event.getUsername(),event.getExpiredOn());

    }
}
