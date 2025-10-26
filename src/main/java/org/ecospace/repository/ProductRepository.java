package org.ecospace.repository;

import org.ecospace.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductRepository  extends JpaRepository<Product, UUID > {

  List<Product>findAllByExpiredBetween(LocalDateTime today, LocalDateTime warningDate);

}
