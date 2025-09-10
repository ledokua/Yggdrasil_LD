package net.ledok.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientReputationData {
    private static final Map<UUID, Integer> CLIENT_REPUTATION_CACHE = new ConcurrentHashMap<>();

    public static void setReputation(UUID playerUuid, int reputation) {
        CLIENT_REPUTATION_CACHE.put(playerUuid, reputation);
    }

    public static int getReputation(UUID playerUuid) {
        return CLIENT_REPUTATION_CACHE.getOrDefault(playerUuid, 0);
    }

    public static void clear() {
        CLIENT_REPUTATION_CACHE.clear();
    }
}
