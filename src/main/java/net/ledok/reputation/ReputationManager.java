package net.ledok.reputation;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ledok.networking.ModPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ReputationManager {

    private static ReputationState getState(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        return ReputationState.getServerState(overworld);
    }

    public static int getReputation(PlayerEntity player) {
        if (player.getServer() == null) return 0;
        ReputationState serverState = getState(player.getServer());
        return serverState.getReputation(player.getUuid());
    }

    public static void setReputation(PlayerEntity player, int amount) {
        if (player.getServer() == null) return;
        ReputationState serverState = getState(player.getServer());
        serverState.setReputation(player.getUuid(), amount);
        syncReputation(player.getServer(), (ServerPlayerEntity) player);
    }

    public static void addReputation(PlayerEntity player, int amount) {
        setReputation(player, getReputation(player) + amount);
    }

    public static void removeReputation(PlayerEntity player, int amount) {
        setReputation(player, getReputation(player) - amount);
    }

    public static void syncReputation(MinecraftServer server, ServerPlayerEntity playerToSync) {
        ModPackets.ReputationSyncPayload payload = new ModPackets.ReputationSyncPayload(
                playerToSync.getUuid(),
                getReputation(playerToSync)
        );

        // Надсилаємо пакет кожному гравцю на сервері, використовуючи ServerPlayNetworking
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}

