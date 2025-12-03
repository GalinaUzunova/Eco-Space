package org.ecospace.repository;

import org.ecospace.model.Product;

import org.ecospace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    Optional<User> findByUsername(String username);


    @Query("select p.productList  from User as p where p.id= :id")
    List<Product> findUserSubs(@Param("id") UUID id);


    @Query("SELECT u FROM User u LEFT JOIN FETCH u.productList p ORDER BY u.username")
    List<User> findAllByAndProductList();

    List<User> getAllBy();


}
