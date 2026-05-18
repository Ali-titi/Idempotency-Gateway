package org.spring.example.idempotencygateway.service;

import org.spring.example.idempotencygateway.dto.PaymentRequest;
import org.spring.example.idempotencygateway.dto.PaymentResponse;
import org.spring.example.idempotencygateway.entity.IdempotencyEntity;
import org.spring.example.idempotencygateway.exception.ConflictException;
import org.spring.example.idempotencygateway.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IdempotencyRepository repository;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, CompletableFuture<PaymentResponse>> processingMap =
            new ConcurrentHashMap<>();

    public PaymentResponse processPayment(String key, PaymentRequest request) throws Exception {

        // 1. Convert request to JSON + hash it
        String requestJson = objectMapper.writeValueAsString(request);
        String requestHash = generateHash(requestJson);

        // 2. Check DB for existing key
        Optional<IdempotencyEntity> existingRecord =
                repository.findByIdempotencyKey(key);

        if (existingRecord.isPresent()) {

            IdempotencyEntity entity = existingRecord.get();

            // 3. If same key but different request → conflict
            if (entity.getRequestHash() != null &&
                    !entity.getRequestHash().equals(requestHash)) {

                throw new ConflictException(
                        "Idempotency key already used for a different request body"
                );
            }

            // 4. If already completed → return cached response
            if (Boolean.TRUE.equals(entity.getCompleted())) {

                return new PaymentResponse(
                        entity.getResponseBody(),
                        "SUCCESS"
                );
            }
        }

        // 5. Handle concurrent request (same key in progress)
        if (processingMap.containsKey(key)) {
            return processingMap.get(key).join();
        }

        CompletableFuture<PaymentResponse> future = new CompletableFuture<>();
        processingMap.put(key, future);

        try {

            // 6. Save initial record (processing)
            IdempotencyEntity entity = new IdempotencyEntity();
            entity.setIdempotencyKey(key);
            entity.setRequestHash(requestHash);
            entity.setProcessing(true);
            entity.setCompleted(false);
            entity.setCreatedAt(LocalDateTime.now());

            repository.save(entity);

            // 7. Simulate payment processing
            Thread.sleep(2000);

            String responseMessage =
                    "Charged " + request.getAmount() + " " + request.getCurrency();

            PaymentResponse response = new PaymentResponse(
                    responseMessage,
                    "SUCCESS"
            );

            // 8. Update DB with result
            entity.setResponseBody(responseMessage);
            entity.setStatusCode(HttpStatus.CREATED.value());
            entity.setProcessing(false);
            entity.setCompleted(true);

            repository.save(entity);

            // 9. Complete future (for concurrent requests)
            future.complete(response);

            return response;

        } finally {
            processingMap.remove(key);
        }
    }

    // 10. Hash function (safe + compatible with all Java versions)
    private String generateHash(String input) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }
}