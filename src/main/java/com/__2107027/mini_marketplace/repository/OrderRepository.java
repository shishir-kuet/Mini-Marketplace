package com.__2107027.mini_marketplace.repository;

import com.__2107027.mini_marketplace.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerId(Long buyerId);
    List<Order> findByStatus(String status);
    List<Order> findByIdIn(Collection<Long> ids);
}
