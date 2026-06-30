package com.attendance.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Strategy for blacklisting JWT tokens by their JTI (JWT ID).
 * <p>Used during logout to prevent a token from being used before its natural expiry.
 * The default implementation (InMemoryTokenBlacklistService) uses a ConcurrentHashMap
 * with a scheduled cleanup thread. Switch to Redis by providing a different bean
 * and setting app.token-blacklist=redis.
 * Layer: security.</p>
 */
public interface TokenBlacklistService {
    void blacklist(String jti, long ttlSec);
    boolean isBlacklisted(String jti);
}

/**
 * In-memory token blacklist with automatic TTL-based cleanup.
 * <p>Entries are stored with their expiry timestamp and cleaned every minute
 * by a scheduled background thread. Not persisted across restarts.
 * Layer: security.</p>
 */
@Service
@ConditionalOnProperty(name = "app.token-blacklist", havingValue = "memory", matchIfMissing = true)
class InMemoryTokenBlacklistService implements TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    InMemoryTokenBlacklistService() {
        cleaner.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            blacklist.entrySet().removeIf(e -> e.getValue() < now);
        }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void blacklist(String jti, long ttlSec) {
        blacklist.put(jti, System.currentTimeMillis() + ttlSec * 1000);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        Long expiry = blacklist.get(jti);
        return expiry != null && expiry > System.currentTimeMillis();
    }
}
