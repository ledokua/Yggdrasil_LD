package net.ledok.reputation;

import net.ledok.Yggdrasil_ld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReputationState extends PersistentState {

    private final Map<UUID, Integer> playerReputations = new HashMap<>();
    // Structure: Attacker UUID -> (Victim UUID -> Timestamp of Kill)
    private final Map<UUID, Map<UUID, Long>> recentKills = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        // Save reputations
        NbtCompound reputationsNbt = new NbtCompound();
        playerReputations.forEach((uuid, reputation) -> reputationsNbt.putInt(uuid.toString(), reputation));
        nbt.put("reputations", reputationsNbt);

        // Save recent kills
        NbtCompound recentKillsNbt = new NbtCompound();
        recentKills.forEach((attackerUuid, victimMap) -> {
            NbtCompound victimNbt = new NbtCompound();
            victimMap.forEach((victimUuid, timestamp) -> victimNbt.putLong(victimUuid.toString(), timestamp));
            recentKillsNbt.put(attackerUuid.toString(), victimNbt);
        });
        nbt.put("recent_kills", recentKillsNbt);

        return nbt;
    }

    public static ReputationState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        ReputationState state = new ReputationState();

        // Load reputations
        NbtCompound reputationsNbt = tag.getCompound("reputations");
        for (String key : reputationsNbt.getKeys()) {
            state.playerReputations.put(UUID.fromString(key), reputationsNbt.getInt(key));
        }

        // Load recent kills
        NbtCompound recentKillsNbt = tag.getCompound("recent_kills");
        for (String attackerKey : recentKillsNbt.getKeys()) {
            UUID attackerUuid = UUID.fromString(attackerKey);
            NbtCompound victimMapNbt = recentKillsNbt.getCompound(attackerKey);
            Map<UUID, Long> victimMap = new HashMap<>();
            for (String victimKey : victimMapNbt.getKeys()) {
                victimMap.put(UUID.fromString(victimKey), victimMapNbt.getLong(victimKey));
            }
            state.recentKills.put(attackerUuid, victimMap);
        }

        return state;
    }

    public static final Type<ReputationState> Type = new Type<>(
            ReputationState::new,
            ReputationState::createFromNbt,
            null
    );

    public static ReputationState getServerState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(Type, Yggdrasil_ld.MOD_ID + "_reputation");
    }

    // --- Reputation Methods ---
    public int getReputation(UUID playerUuid) {
        return playerReputations.getOrDefault(playerUuid, 0);
    }

    public void setReputation(UUID playerUuid, int amount) {
        int cappedAmount = Math.max(-100000, Math.min(1000000, amount));
        playerReputations.put(playerUuid, cappedAmount);
        markDirty();
    }

    // --- Cooldown Methods ---
    public boolean wasRecentlyKilledBy(UUID attackerUuid, UUID victimUuid, long currentTime, int cooldownTicks) {
        if (!recentKills.containsKey(attackerUuid)) {
            return false;
        }
        Map<UUID, Long> victimMap = recentKills.get(attackerUuid);
        if (!victimMap.containsKey(victimUuid)) {
            return false;
        }
        long lastKillTime = victimMap.get(victimUuid);
        return (currentTime - lastKillTime) < cooldownTicks;
    }

    public void recordKill(UUID attackerUuid, UUID victimUuid, long timestamp) {
        recentKills.computeIfAbsent(attackerUuid, k -> new HashMap<>()).put(victimUuid, timestamp);
        markDirty();
    }
}
