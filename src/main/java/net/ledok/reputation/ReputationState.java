package net.ledok.reputation;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReputationState extends PersistentState {

    private final Map<UUID, Integer> playerReputations = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound reputationsNbt = new NbtCompound();
        playerReputations.forEach((uuid, reputation) -> {
            reputationsNbt.putInt(uuid.toString(), reputation);
        });
        nbt.put("reputations", reputationsNbt);
        return nbt;
    }

    public static ReputationState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        ReputationState state = new ReputationState();
        NbtCompound reputationsNbt = tag.getCompound("reputations");
        for (String key : reputationsNbt.getKeys()) {
            state.playerReputations.put(UUID.fromString(key), reputationsNbt.getInt(key));
        }
        return state;
    }

    public static final Type<ReputationState> Type = new Type<>(
            ReputationState::new,
            ReputationState::createFromNbt,
            null
    );

    public static ReputationState getServerState(ServerWorld world) {
        PersistentStateManager persistentStateManager = world.getPersistentStateManager();
        return persistentStateManager.getOrCreate(Type, "yggdrasil_reputation");
    }

    public int getReputation(UUID playerUuid) {
        return playerReputations.getOrDefault(playerUuid, 0);
    }

    public void setReputation(UUID playerUuid, int amount) {
        int cappedAmount = Math.max(-100000, Math.min(100000, amount));
        playerReputations.put(playerUuid, cappedAmount);
        markDirty();
    }

    // --- ДОДАЙТЕ ЦЕЙ МЕТОД ---
    public Map<UUID, Integer> getAllReputations() {
        // Повертає копію мапи, яку не можна змінити, для безпеки
        return Collections.unmodifiableMap(playerReputations);
    }
}

