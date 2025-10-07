package org.ecospace.repository;

import org.ecospace.model.Subscription;
import org.ecospace.model.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {


 Optional<Subscription>findById(UUID id);
 List<Subscription>getByType(SubscriptionType type);
 List<Subscription>findAll();







}


