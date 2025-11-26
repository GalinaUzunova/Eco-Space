package org.ecospace.repository;

import org.ecospace.model.Product;
import org.ecospace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProductRepository  extends JpaRepository<Product, UUID > {

  List<Product>findAllByExpiredBetween(LocalDateTime today, LocalDateTime warningDate);
  List<Product>findAllByExpiredBeforeAndActiveTrue(LocalDateTime date);



}
