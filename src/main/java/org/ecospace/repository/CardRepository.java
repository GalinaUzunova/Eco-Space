package org.ecospace.repository;

import org.ecospace.model.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardRepository  extends JpaRepository<UserCard, UUID> {
}
