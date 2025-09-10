package net.ledok.util;

import java.util.UUID;

public class PvPContextManager {
    // ThreadLocal гарантує, що дані для кожного гравця будуть ізольовані,
    // що важливо на багатокористувацькому сервері.
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
