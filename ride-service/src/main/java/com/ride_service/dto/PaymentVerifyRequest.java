package com.ride_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyRequest {
    private String rideId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
