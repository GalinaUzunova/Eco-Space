package org.ecospace.model.dto;


import lombok.experimental.UtilityClass;

import org.ecospace.model.Subscription;

@UtilityClass
public class DtoMapper {

    public static EditSubDto fromSubscription(Subscription subscription){
     EditSubDto eidted=new EditSubDto();
     eidted.setNamePackage(subscription.getNamePackage());
     eidted.setPrice(subscription.getPrice());
     eidted.setDescription(subscription.getDescription());
     eidted.setType(subscription.getType());
     eidted.setId(subscription.getId());


        return eidted;

    }

}
