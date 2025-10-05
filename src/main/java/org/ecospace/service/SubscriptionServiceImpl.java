package org.ecospace.service;


import org.ecospace.model.Subscription;
import org.ecospace.model.SubscriptionType;
import org.ecospace.model.dto.*;
import org.ecospace.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionServiceImpl {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public void addNewSubscription(AddSubDto subDto) {


        Subscription subscription =
                Subscription.builder()
                        .type(subDto.getType())
                        .price(subDto.getPrice())
                        .namePackage(subDto.getNamePackage())
                        .createdOn(LocalDateTime.now())
                        .isActive(true)
                        .description(subDto.getDescription())
                        .build();

        this.subscriptionRepository.save(subscription);


    }

    public List<MaintenanceSubDto> getMaintenanceSubscriptions() {
        List<MaintenanceSubDto>byMaintanace=new ArrayList<>();
        List<Subscription> maintanaceSubscriptions = subscriptionRepository.getByType(SubscriptionType.MAINTANACE);
        if (!maintanaceSubscriptions.isEmpty()) {
            maintanaceSubscriptions.forEach(m->{
                MaintenanceSubDto dto=new MaintenanceSubDto();
                dto.setId(m.getId());
                dto.setDescription(m.getNamePackage());
                dto.setPrice(m.getPrice());
                dto.setDescription(m.getDescription());
                dto.setNamePackage(m.getNamePackage());
                byMaintanace.add(dto);

            });

            return byMaintanace;
        }
        throw new RuntimeException("No subscriptions");
    }



    public List<String> getSubscriptionName() {
        List<Subscription> activeSubscriptions = subscriptionRepository.findAll();
        List<String> subscriptionNames = new ArrayList<>();
        if (!activeSubscriptions.isEmpty()) {
            activeSubscriptions.forEach(s -> {
                String name = s.getNamePackage();
                subscriptionNames.add(name);
            });


        }
        return subscriptionNames;

    }

    @Transactional
    public void editSubscription(UUID id, EditSubDto edited) {

        Optional<Subscription> byId = this.subscriptionRepository.findById(id);
        if (byId.isPresent()) {
            Subscription subscription = byId.get();

            subscription.setType(edited.getType());
            subscription.setNamePackage(edited.getNamePackage());
            subscription.setSubscriptionPeriod(edited.getSubscriptionPeriod());
            subscription.setPrice(edited.getPrice());
            subscription.setDescription(edited.getDescription());
            subscription.setActive(true);

            this.subscriptionRepository.save(subscription);

        }
    }

    public Subscription byId(UUID id) {
        Optional<Subscription> byId = this.subscriptionRepository.findById(id);
        if (byId.isPresent()) {
            return byId.get();

        }
        throw new RuntimeException("Package dost exist");
    }

    public List<DesignSubscriptionDto>getDesignSubscriptions() {
        List<DesignSubscriptionDto>designSubs=new ArrayList<>();
        List<Subscription> designList = this.subscriptionRepository.getByType(SubscriptionType.DESIGN);

        if (!designList.isEmpty()) {
            designList.forEach(d->{
                DesignSubscriptionDto dto=new DesignSubscriptionDto();
                dto.setNamePackage(d.getNamePackage());
                dto.setId(d.getId());
                dto.setPrice(d.getPrice());
                dto.setDescription(d.getDescription());
                designSubs.add(dto);

            });
            return designSubs;
        }
        return null;
    }

    public void delete(UUID id){

        Optional<Subscription>subscription=subscriptionRepository.findById(id);

        subscription.ifPresent(subscriptionRepository::delete);

    }

    public List<Subscription>exsistingList(){

        return this.subscriptionRepository.findAll();
    }

    public Subscription byName(String name){

    Optional<Subscription>byName=this.subscriptionRepository.findByNamePackage(name);
    if(byName.isEmpty()){
        throw new RuntimeException("No such package");
    }
    return byName.get();
    }


    public List<Subscription> getAllSubscriptionByUserId(UUID id) {

     return this.subscriptionRepository.findAllByClientId(id);

    }
}
