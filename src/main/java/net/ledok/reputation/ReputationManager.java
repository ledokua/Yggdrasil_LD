package net.ledok.reputation;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ledok.Yggdrasil_ld;
import net.ledok.networking.ModPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.UUID;

public class ReputationManager {

    private static ReputationState getState(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        return ReputationState.getServerState(overworld);
    }

    //For leaderboard
    public static Map<UUID, Integer> getAllReputations(MinecraftServer server) {
        return getState(server).getAllReputations();
    }

    public static int getReputation(PlayerEntity player) {
        if (player.getServer() == null) return 0;
        return getState(player.getServer()).getReputation(player.getUuid());
    }

    public static void setReputation(PlayerEntity player, int amount) {
        if (player.getServer() == null) return;
        getState(player.getServer()).setReputation(player.getUuid(), amount);
        syncReputationWithAll(player.getServer(), (ServerPlayerEntity) player);
    }

    public static void addReputation(PlayerEntity player, int amount) {
        setReputation(player, getReputation(player) + amount);
    }

    public static void removeReputation(PlayerEntity player, int amount) {
        setReputation(player, getReputation(player) - amount);
    }

    // --- COOLDOWN METHODS ---
    public static boolean wasRecentlyKilledBy(MinecraftServer server, UUID attacker, UUID victim) {
        long currentTime = server.getOverworld().getTime();
        int cooldown = Yggdrasil_ld.CONFIG.pvp_cooldown_ticks;
        return getState(server).wasRecentlyKilledBy(attacker, victim, currentTime, cooldown);
    }

    public static void recordKill(MinecraftServer server, UUID attacker, UUID victim) {
        long timestamp = server.getOverworld().getTime();
        getState(server).recordKill(attacker, victim, timestamp);
    }


    // --- Networking Methods ---
    public static void syncReputationWithAll(MinecraftServer server, ServerPlayerEntity playerToSync) {
        ModPackets.ReputationSyncPayload payload = new ModPackets.ReputationSyncPayload(
                playerToSync.getUuid(),
                getReputation(playerToSync)
        );
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
