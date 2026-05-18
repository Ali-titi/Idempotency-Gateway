package org.spring.example.idempotencygateway.repository;

import org.spring.example.idempotencygateway.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyEntity, Long> {

    Optional<IdempotencyEntity> findByIdempotencyKey(String idempotencyKey);
}