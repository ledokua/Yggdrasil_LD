package net.ledok.reputation;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ledok.networking.ModPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

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
        syncReputationWithAll(player.getServer(), (ServerPlayerEntity) player);
    }

    public static void addReputation(PlayerEntity player, int amount) {
        setReputation(player, getReputation(player) + amount);
    }

    public static void removeReputation(PlayerEntity player, int amount) {
        setReputation(player, getReputation(player) - amount);
    }

    public static void syncReputationWithAll(MinecraftServer server, ServerPlayerEntity playerToSync) {
        ModPackets.ReputationSyncPayload payload = new ModPackets.ReputationSyncPayload(
                playerToSync.getUuid(),
                getReputation(playerToSync)
        );
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    // ВИПРАВЛЕНО: Внутрішній клас зроблено `public static`, щоб він був видимий ззовні
    public static class ReputationEntry {
        public final String name;
        public final int reputation;

        public ReputationEntry(String name, int reputation) {
            this.name = name;
            this.reputation = reputation;
        }
    }

    // ВИПРАВЛЕНО: Метод `getRankings` тепер існує і є `public static`
    public static List<ReputationEntry> getRankings(MinecraftServer server, boolean ascending) {
        ReputationState state = getState(server);
        Map<UUID, Integer> allReputations = state.getAllReputations();
        List<ReputationEntry> entries = new ArrayList<>();

        allReputations.forEach((uuid, reputation) -> {
            Optional<GameProfile> profile = server.getUserCache().getByUuid(uuid);
            String name = profile.map(GameProfile::getName).orElse("Unknown");
            entries.add(new ReputationEntry(name, reputation));
        });

        if (ascending) {
            entries.sort(Comparator.comparingInt(entry -> entry.reputation)); // Сортування за зростанням (для негативних)
        } else {
            entries.sort((a, b) -> Integer.compare(b.reputation, a.reputation)); // Сортування за спаданням (для позитивних)
        }
        return entries;
    }
}

