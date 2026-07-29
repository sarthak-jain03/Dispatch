package com.ride_service.service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import com.ride_service.dto.PaymentResponse;
import com.ride_service.entity.PaymentStatus;
import com.ride_service.entity.Ride;
import com.ride_service.entity.RideStatus;
import com.ride_service.entity.SagaStep;
import com.ride_service.repository.RideRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RideRepository rideRepository;
    private final SagaEventService sagaEventService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            log.info("Razorpay client initialized with key: {}",
                    razorpayKeyId.substring(0, Math.min(12, razorpayKeyId.length())) + "...");
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
            throw new RuntimeException("Razorpay initialization failed", e);
        }
    }

    /**
     * Creates a Razorpay Order for the given ride.
     * Called when ride is completed and payment is pending.
     */
    @Transactional
    public PaymentResponse createOrder(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));

        // If order already exists, return it (idempotent)
        if (ride.getPaymentOrderId() != null && ride.getPaymentStatus() == PaymentStatus.ORDER_CREATED) {
            log.info("Returning existing Razorpay order for ride {}", rideId);
            return new PaymentResponse(
                    ride.getPaymentOrderId(),
                    (int) (ride.getActualFare() > 0 ? ride.getActualFare() * 100 : ride.getEstimatedFare() * 100),
                    "INR",
                    razorpayKeyId
            );
        }

        double fareAmount = ride.getActualFare() > 0 ? ride.getActualFare() : ride.getEstimatedFare();
        int amountInPaise = (int) (fareAmount * 100); // Razorpay expects amount in paise

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "r_" + rideId);
            orderRequest.put("notes", new JSONObject()
                    .put("rideId", rideId)
                    .put("userId", ride.getUserId()));

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");

            ride.setPaymentOrderId(orderId);
            ride.setPaymentStatus(PaymentStatus.ORDER_CREATED);
            ride.setStatus(RideStatus.PAYMENT_PENDING);
            rideRepository.save(ride);

            sagaEventService.recordStep(rideId, SagaStep.PAYMENT_ORDER_CREATED, "COMPLETED",
                    "Razorpay orderId=" + orderId + ", amount=" + amountInPaise);

            log.info("Razorpay order created for ride {}: orderId={}, amount=₹{}",
                    rideId, orderId, fareAmount);

            return new PaymentResponse(orderId, amountInPaise, "INR", razorpayKeyId);

        } catch (RazorpayException e) {
            sagaEventService.recordFailure(rideId, SagaStep.PAYMENT_ORDER_CREATED, e.getMessage());
            log.error("Failed to create Razorpay order for ride {}: {}", rideId, e.getMessage());
            throw new RuntimeException("Payment order creation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies the Razorpay payment signature using HMAC-SHA256.
     * This ensures the payment response hasn't been tampered with.
     */
    @Transactional
    public boolean verifyPayment(String rideId, String razorpayOrderId,
                                 String razorpayPaymentId, String razorpaySignature) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));

        // Verify that the order ID matches what we created
        if (!razorpayOrderId.equals(ride.getPaymentOrderId())) {
            log.error("Order ID mismatch for ride {}: expected={}, got={}",
                    rideId, ride.getPaymentOrderId(), razorpayOrderId);
            sagaEventService.recordFailure(rideId, SagaStep.PAYMENT_VERIFIED,
                    "Order ID mismatch");
            return false;
        }

        // Verify HMAC signature
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        boolean isValid = verifySignature(payload, razorpaySignature, razorpayKeySecret);

        if (isValid) {
            ride.setPaymentId(razorpayPaymentId);
            ride.setPaymentStatus(PaymentStatus.COMPLETED);
            ride.setStatus(RideStatus.PAYMENT_COMPLETED);
            rideRepository.save(ride);

            sagaEventService.recordStep(rideId, SagaStep.PAYMENT_VERIFIED, "COMPLETED",
                    "paymentId=" + razorpayPaymentId);

            log.info("Payment verified for ride {}: paymentId={}", rideId, razorpayPaymentId);
        } else {
            ride.setPaymentStatus(PaymentStatus.FAILED);
            ride.setStatus(RideStatus.PAYMENT_FAILED);
            rideRepository.save(ride);

            sagaEventService.recordFailure(rideId, SagaStep.PAYMENT_VERIFIED,
                    "Signature verification failed");

            log.error("Payment signature verification failed for ride {}", rideId);
        }

        return isValid;
    }

    /**
     * Initiates a refund for a completed payment (compensation action).
     */
    @Transactional
    public void initiateRefund(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));

        if (ride.getPaymentId() == null) {
            log.warn("No payment to refund for ride {}", rideId);
            return;
        }

        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("payment_id", ride.getPaymentId());

            Refund refund = razorpayClient.payments.refund(ride.getPaymentId(), refundRequest);

            ride.setPaymentStatus(PaymentStatus.REFUNDED);
            rideRepository.save(ride);

            sagaEventService.recordCompensation(rideId, SagaStep.PAYMENT_VERIFIED,
                    "Refund initiated: refundId=" + refund.get("id"));

            log.info("Refund initiated for ride {}: refundId={}", rideId, refund.get("id"));

        } catch (RazorpayException e) {
            log.error("Refund failed for ride {}: {}", rideId, e.getMessage());
            sagaEventService.recordFailure(rideId, SagaStep.PAYMENT_VERIFIED,
                    "Refund failed: " + e.getMessage());
        }
    }

    /**
     * Get the current payment status for a ride.
     */
    public PaymentStatus getPaymentStatus(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));
        return ride.getPaymentStatus();
    }

    /**
     * HMAC-SHA256 signature verification.
     */
    private boolean verifySignature(String payload, String expectedSignature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = toHexString(hash);

            return generatedSignature.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Error verifying signature: {}", e.getMessage());
            return false;
        }
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String hex = formatter.toString();
        formatter.close();
        return hex;
    }
}
