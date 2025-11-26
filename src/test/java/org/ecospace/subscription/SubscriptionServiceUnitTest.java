package org.ecospace.subscription;

import org.ecospace.exception.SubscriptionNotFoundException;
import org.ecospace.model.Subscription;
import org.ecospace.model.SubscriptionType;
import org.ecospace.model.dto.AddSubDto;
import org.ecospace.model.dto.EditSubDto;
import org.ecospace.repository.SubscriptionRepository;
import org.ecospace.service.SubscriptionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.bind.ValidationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceUnitTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    void addNewSubscription_whenAllParametersAreIn_andSaveItToTheDb()  {
        AddSubDto dto = AddSubDto.builder()
                .type(SubscriptionType.MAINTANACE)
                .price(1500.00)
                .namePackage("Monthly")
                .description("Monthly maintenance")
                .build();

        subscriptionService.addNewSubscription(dto);
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        Subscription savedSubscription = captor.getValue();
        assertThat(savedSubscription.getType()).isEqualTo(SubscriptionType.MAINTANACE);
        assertThat(savedSubscription.getPrice()).isEqualTo(1500);
        assertThat(savedSubscription.getNamePackage()).isEqualTo("Monthly");
        assertThat(savedSubscription.getDescription()).isEqualTo("Monthly maintenance");
    }

    @Test
    void editSubscription_whenSubscriptionExist_thenReturnUpdatedSub() {
        UUID id = UUID.randomUUID();
        EditSubDto dto = EditSubDto.builder()
                .type(SubscriptionType.DESIGN)
                .namePackage("2D plan")
                .price(2500.00)
                .description("2D plans and site elevation")
                .build();

        Subscription retrievFromDb = Subscription.builder()

                .type(SubscriptionType.DESIGN)
                .namePackage("2D plan")
                .price(1500.00)
                .description("2D plans ")

                .build();


        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(retrievFromDb));

        subscriptionService.editSubscription(id, dto);
        verify(subscriptionRepository).findById(id);
        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);

        verify(subscriptionRepository).save(subscriptionCaptor.capture());

        Subscription savedSubscription = subscriptionCaptor.getValue();

        assertEquals(SubscriptionType.DESIGN, savedSubscription.getType());
        assertEquals("2D plan", savedSubscription.getNamePackage());
        assertEquals(2500.00, savedSubscription.getPrice());
        assertEquals("2D plans and site elevation", savedSubscription.getDescription());


    }

    @Test
    void deleteSubscription_whenExist_andSaveToDb() {
        UUID id = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setId(id);

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));

        subscriptionService.delete(id);
        verify(subscriptionRepository).findById(id);
        verify(subscriptionRepository).delete(subscription);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
   void deleteSubscription_whenSubscriptionNotExist(){
        UUID id = UUID.randomUUID();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

       assertThrows(SubscriptionNotFoundException.class ,()->subscriptionService.delete(id));

        verify(subscriptionRepository, never()).save(any());


    }

}
