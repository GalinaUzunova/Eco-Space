package org.ecospace.repository;

import org.ecospace.model.User;
import org.ecospace.model.dto.LoginDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository  extends JpaRepository<User, UUID> {


    Optional<User> findByUsername(String username);

    Optional<User>findByUsernameAndEmail(String username,String email);




}
