package org.spring.example.idempotencygateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository<IdempotencyEntity> extends JpaRepository<IdempotencyEntity, Long> {

    Optional<IdempotencyEntity> findByIdempotencyKey(String idempotencyKey);
}
