package org.ecospace.model.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ecospace.model.SubscriptionType;


import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class DesignSubscriptionDto {
    private UUID id;
    private SubscriptionType type;
    private String namePackage;

    private Double price;

    private String description;





}
