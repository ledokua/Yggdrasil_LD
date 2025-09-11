package net.ledok.util;

import java.util.UUID;

public class PvPContextManager {
    private static final ThreadLocal<UUID> lastPvpAttacker = new ThreadLocal<>();

    public static void setAttacker(UUID attackerUuid) {
        lastPvpAttacker.set(attackerUuid);
    }

    public static UUID getAttacker() {
        return lastPvpAttacker.get();
    }

    public static void clear() {
        lastPvpAttacker.remove();
    }
}
