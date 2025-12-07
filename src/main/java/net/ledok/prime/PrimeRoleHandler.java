package net.ledok.prime;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ledok.YggdrasilLdMod;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.minecraft.server.level.ServerPlayer;


import java.time.Instant;

import static net.ledok.YggdrasilLdMod.LOGGER;
import static ua.com.minestar.Minestar.minestar;

public class PrimeRoleHandler {

    private static LuckPerms luckPermsApi;

    public static void register() {
        if (!YggdrasilLdMod.CONFIG.prime_role_sync_enabled) {
            LOGGER.info("[Prime Sync] Feature is disabled in the config.");
            return;
        }

        try {
            luckPermsApi = LuckPermsProvider.get();
        } catch (IllegalStateException ex) {
            LOGGER.error("[Prime Sync] LuckPerms API not found. Disabling feature.");
            return;
        }

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            checkPrimeStatus(handler.getPlayer());
        });

        LOGGER.info("[Prime Sync] Feature has been initialized successfully.");
    }

    private static void checkPrimeStatus(ServerPlayer player) {
        LOGGER.info("[Prime Sync] Checking prime status for player {} (UUID: {}).", player.getName().getString(), player.getUUID());

        minestar.getUserByProfileUuid(player.getUUID())
                .onSuccess(minestarUser -> {
                    boolean apiHasPrime = minestarUser.getPrimeStatus()
                            .map(primeStatus -> primeStatus.expiresAt().isAfter(Instant.now()))
                            .orElse(false);

                    User luckPermsUser = luckPermsApi.getUserManager().loadUser(player.getUUID()).join();
                    if (luckPermsUser == null) {
                        LOGGER.warn("[Prime Sync] Could not load LuckPerms user for {}.", player.getName().getString());
                        return;
                    }

                    boolean lpHasPrime = luckPermsUser.getNodes().stream()
                            .filter(n -> n instanceof InheritanceNode)
                            .map(n -> ((InheritanceNode) n).getGroupName())
                            .anyMatch(g -> g.equalsIgnoreCase("prime"));

                    if (apiHasPrime && !lpHasPrime) {
                        LOGGER.info("[Prime Sync] API says user is Prime, but they don't have the role. Adding it now...");
                        luckPermsUser.data().add(InheritanceNode.builder("prime").build());
                        luckPermsApi.getUserManager().saveUser(luckPermsUser);
                        LOGGER.info("[Prime Sync] Added 'Prime' role to {}.", player.getName().getString());
                    } else if (!apiHasPrime && lpHasPrime) {
                        LOGGER.info("[Prime Sync] API says user is NOT Prime, but they have the role. Removing it now...");
                        luckPermsUser.data().remove(InheritanceNode.builder("prime").build());
                        luckPermsApi.getUserManager().saveUser(luckPermsUser);
                        LOGGER.info("[Prime Sync] Removed 'Prime' role from {}.", player.getName().getString());
                    } else {
                        LOGGER.info("[Prime Sync] Prime status for {} is already synced.", player.getName().getString());
                    }
                })
                .onFailure(cause -> {
                    LOGGER.error("[Prime Sync] Failed to get player prime status for {}", player.getName().getString(), cause);
                });
    }
}
