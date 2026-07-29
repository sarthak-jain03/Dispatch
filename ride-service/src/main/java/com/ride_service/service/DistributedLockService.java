package com.ride_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_PREFIX = "ride:lock:";
    private static final Duration DEFAULT_TTL = Duration.ofSeconds(30);

    /**
     * Lua script to release lock only if the current holder matches the ownerId.
     * This prevents a driver from releasing another driver's lock.
     */
    private static final String RELEASE_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end";

    /**
     * Try to acquire a distributed lock for a ride.
     *
     * @param rideId  The ride to lock
     * @param ownerId The driver attempting to claim the ride
     * @return true if lock acquired, false if already locked by another driver
     */
    public boolean tryLock(String rideId, String ownerId) {
        return tryLock(rideId, ownerId, DEFAULT_TTL);
    }

    /**
     * Try to acquire a distributed lock with a custom TTL.
     */
    public boolean tryLock(String rideId, String ownerId, Duration ttl) {
        String lockKey = LOCK_PREFIX + rideId;

        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, ownerId, ttl);

        if (Boolean.TRUE.equals(acquired)) {
            log.info("[LOCK] Acquired lock for ride {} by driver {}", rideId, ownerId);
            return true;
        }

        // Check if we already own the lock (re-entrant)
        String currentOwner = stringRedisTemplate.opsForValue().get(lockKey);
        if (ownerId.equals(currentOwner)) {
            log.info("[LOCK] Driver {} already holds lock for ride {}", ownerId, rideId);
            return true;
        }

        log.warn("[LOCK] Failed to acquire lock for ride {} — held by driver {}", rideId, currentOwner);
        return false;
    }

    /**
     * Release the lock, but only if the caller is the current owner.
     * Uses a Lua script for atomicity.
     */
    public boolean releaseLock(String rideId, String ownerId) {
        String lockKey = LOCK_PREFIX + rideId;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(RELEASE_LOCK_SCRIPT);
        script.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(
                script,
                Collections.singletonList(lockKey),
                ownerId
        );

        boolean released = result != null && result == 1L;

        if (released) {
            log.info("[LOCK] Released lock for ride {} by driver {}", rideId, ownerId);
        } else {
            log.warn("[LOCK] Failed to release lock for ride {} — driver {} is not the owner", rideId, ownerId);
        }

        return released;
    }

    /**
     * Check who currently holds the lock (for debugging/monitoring).
     */
    public String getLockOwner(String rideId) {
        String lockKey = LOCK_PREFIX + rideId;
        return stringRedisTemplate.opsForValue().get(lockKey);
    }
}
