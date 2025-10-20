package net.ledok.reputation;

import net.ledok.YggdrasilLdMod;
// MOJANG MAPPINGS: Update imports for NBT, registry, world, and persistent state.
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// MOJANG MAPPINGS: PersistentState is now SavedData.
public class ReputationState extends SavedData {

    private final Map<UUID, Integer> playerReputations = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> recentKills = new HashMap<>();

    // MOJANG MAPPINGS: writeNbt is now save, NbtCompound is CompoundTag, and RegistryWrapper.WrapperLookup is HolderLookup.Provider.
    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        CompoundTag reputationsNbt = new CompoundTag();
        playerReputations.forEach((uuid, reputation) -> reputationsNbt.putInt(uuid.toString(), reputation));
        nbt.put("reputations", reputationsNbt);

        CompoundTag recentKillsNbt = new CompoundTag();
        recentKills.forEach((attackerUuid, victimMap) -> {
            CompoundTag victimNbt = new CompoundTag();
            victimMap.forEach((victimUuid, timestamp) -> victimNbt.putLong(victimUuid.toString(), timestamp));
            recentKillsNbt.put(attackerUuid.toString(), victimNbt);
        });
        nbt.put("recent_kills", recentKillsNbt);

        return nbt;
    }

    public static ReputationState createFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        ReputationState state = new ReputationState();

        CompoundTag reputationsNbt = tag.getCompound("reputations");
        for (String key : reputationsNbt.getAllKeys()) {
            state.playerReputations.put(UUID.fromString(key), reputationsNbt.getInt(key));
        }

        CompoundTag recentKillsNbt = tag.getCompound("recent_kills");
        for (String attackerKey : recentKillsNbt.getAllKeys()) {
            UUID attackerUuid = UUID.fromString(attackerKey);
            CompoundTag victimMapNbt = recentKillsNbt.getCompound(attackerKey);
            Map<UUID, Long> victimMap = new HashMap<>();
            for (String victimKey : victimMapNbt.getAllKeys()) {
                victimMap.put(UUID.fromString(victimKey), victimMapNbt.getLong(victimKey));
            }
            state.recentKills.put(attackerUuid, victimMap);
        }

        return state;
    }

    // MOJANG MAPPINGS: The PersistentState.Type is now SavedData.Factory.
    public static final SavedData.Factory<ReputationState> Type = new SavedData.Factory<>(
            ReputationState::new,
            ReputationState::createFromNbt,
            null // DataFixTypes can be null for mods.
    );

    // MOJANG MAPPINGS: ServerWorld is now ServerLevel, and getPersistentStateManager is now getDataStorage.
    public static ReputationState getServerState(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(Type, YggdrasilLdMod.MOD_ID + "_reputation");
    }

    public int getReputation(UUID playerUuid) {
        return playerReputations.getOrDefault(playerUuid, 0);
    }

    public void setReputation(UUID playerUuid, int amount) {
        int cappedAmount = Math.max(-1000000, Math.min(1000000, amount));
        playerReputations.put(playerUuid, cappedAmount);
        setDirty(); // MOJANG MAPPINGS: markDirty() is now setDirty().
    }

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
        setDirty();
    }

    public Map<UUID, Integer> getAllReputations() {
        return new HashMap<>(playerReputations);
    }
}
