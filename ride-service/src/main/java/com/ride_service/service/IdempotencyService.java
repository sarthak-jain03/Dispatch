package com.ride_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String IDEMPOTENCY_PREFIX = "idempotency:ride:";
    private static final Duration TTL = Duration.ofHours(24);

    /**
     * Checks if this idempotency key has been seen before.
     * Uses Redis SETNX (SET if Not eXists) with a 24h TTL.
     *
     * @return true if this is the FIRST time seeing this key (proceed with request)
     *         false if the key already exists (duplicate request)
     */
    public boolean checkAndMark(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;

        Boolean wasSet = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", TTL);

        if (Boolean.TRUE.equals(wasSet)) {
            log.debug("Idempotency key [{}] is new — proceeding", idempotencyKey);
            return true;
        }

        log.warn("Idempotency key [{}] already exists — duplicate request", idempotencyKey);
        return false;
    }

    /**
     * Remove an idempotency key (used during compensation/rollback).
     */
    public void remove(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;
        stringRedisTemplate.delete(redisKey);
        log.debug("Idempotency key [{}] removed", idempotencyKey);
    }

    /**
     * Generate a deterministic idempotency key from ride request parameters.
     * This prevents the same user from booking the exact same ride twice.
     */
    public static String generateKey(String userId, double pickupLat, double pickupLng,
                                     double dropLat, double dropLng) {
        String raw = String.format("%s:%.6f:%.6f:%.6f:%.6f",
                userId, pickupLat, pickupLng, dropLat, dropLng);
        return String.valueOf(raw.hashCode());
    }
}
