package org.spring.example.idempotencygateway.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
@Data
public class IdempotencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String requestHash;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    private Integer statusCode;

    private Boolean processing;

    private Boolean completed;

    private LocalDateTime createdAt;
}
