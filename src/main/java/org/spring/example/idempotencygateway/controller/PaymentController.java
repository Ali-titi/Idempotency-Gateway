package org.spring.example.idempotencygateway.controller;

import org.spring.example.idempotencygateway.dto.PaymentRequest;
import org.spring.example.idempotencygateway.dto.PaymentResponse;
import org.spring.example.idempotencygateway.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody PaymentRequest request
    ) {

        try {

            PaymentResponse response = paymentService.processPayment(key, request);

            HttpHeaders headers = new HttpHeaders();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(response);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}