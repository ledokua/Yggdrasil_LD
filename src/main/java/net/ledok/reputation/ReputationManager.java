package net.ledok.reputation;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ledok.YggdrasilLdMod;
import net.ledok.networking.ModPackets;
// MOJANG MAPPINGS: Update entity and world imports.
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.UUID;

public class ReputationManager {

    private static ReputationState getState(MinecraftServer server) {
        // MOJANG MAPPINGS: getOverworld() is now overworld(). ServerWorld is now ServerLevel.
        ServerLevel overworld = server.overworld();
        return ReputationState.getServerState(overworld);
    }

    public static Map<UUID, Integer> getAllReputations(MinecraftServer server) {
        return getState(server).getAllReputations();
    }

    // MOJANG MAPPINGS: PlayerEntity is now Player.
    public static int getReputation(Player player) {
        if (player.getServer() == null) return 0;
        return getState(player.getServer()).getReputation(player.getUUID());
    }

    public static void setReputation(Player player, int amount) {
        if (player.getServer() == null) return;
        getState(player.getServer()).setReputation(player.getUUID(), amount);
        // MOJANG MAPPINGS: Cast to ServerPlayer instead of ServerPlayerEntity.
        syncReputationWithAll(player.getServer(), (ServerPlayer) player);
    }

    public static void addReputation(Player player, int amount) {
        setReputation(player, getReputation(player) + amount);
    }

    public static void removeReputation(Player player, int amount) {
        setReputation(player, getReputation(player) - amount);
    }

    public static boolean wasRecentlyKilledBy(MinecraftServer server, UUID attacker, UUID victim) {
        long currentTime = server.overworld().getGameTime();
        int cooldown = YggdrasilLdMod.CONFIG.pvp_cooldown_ticks;
        return getState(server).wasRecentlyKilledBy(attacker, victim, currentTime, cooldown);
    }

    public static void recordKill(MinecraftServer server, UUID attacker, UUID victim) {
        long timestamp = server.overworld().getGameTime();
        getState(server).recordKill(attacker, victim, timestamp);
    }

    // MOJANG MAPPINGS: Update method signature to use ServerPlayer.
    public static void syncReputationWithAll(MinecraftServer server, ServerPlayer playerToSync) {
        ModPackets.ReputationSyncPayload payload = new ModPackets.ReputationSyncPayload(
                playerToSync.getUUID(),
                getReputation(playerToSync)
        );
        // MOJANG MAPPINGS: getPlayerManager() is getPlayerList(), and getPlayerList() is getPlayers().
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
