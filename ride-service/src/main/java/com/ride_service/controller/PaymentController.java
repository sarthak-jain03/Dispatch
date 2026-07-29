package com.ride_service.controller;

import com.ride_service.dto.PaymentRequest;
import com.ride_service.dto.PaymentResponse;
import com.ride_service.dto.PaymentVerifyRequest;
import com.ride_service.entity.PaymentStatus;
import com.ride_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponse> createOrder(@RequestBody PaymentRequest request) {
        log.info("Creating Razorpay order for ride: {}", request.getRideId());
        PaymentResponse response = paymentService.createOrder(request.getRideId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyPayment(@RequestBody PaymentVerifyRequest request) {
        log.info("Verifying payment for ride: {}", request.getRideId());
        boolean success = paymentService.verifyPayment(
                request.getRideId(),
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );
        return ResponseEntity.ok(success);
    }

    @GetMapping("/{rideId}/status")
    public ResponseEntity<PaymentStatus> getPaymentStatus(@PathVariable String rideId) {
        PaymentStatus status = paymentService.getPaymentStatus(rideId);
        return ResponseEntity.ok(status);
    }
}
