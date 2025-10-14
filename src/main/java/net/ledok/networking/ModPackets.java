package net.ledok.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ledok.YggdrasilLdMod;
import net.ledok.block.entity.BossSpawnerBlockEntity;
import net.ledok.block.entity.MobSpawnerBlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class ModPackets {

    public record ReputationSyncPayload(UUID playerUuid, int reputation) implements CustomPayload {
        public static final Id<ReputationSyncPayload> ID = new Id<>(Identifier.of(YggdrasilLdMod.MOD_ID, "reputation_sync"));
        public static final PacketCodec<PacketByteBuf, ReputationSyncPayload> CODEC = PacketCodec.of(ReputationSyncPayload::write, ReputationSyncPayload::new);

        public ReputationSyncPayload(PacketByteBuf buf) {
            this(buf.readUuid(), buf.readInt());
        }

        public void write(PacketByteBuf buf) {
            buf.writeUuid(playerUuid);
            buf.writeInt(reputation);
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record UpdateBossSpawnerPayload(
            BlockPos pos, String mobId, int respawnTime, int portalTime, String lootTable,
            BlockPos exitCoords, BlockPos enterSpawnCoords, BlockPos enterDestCoords,
            int triggerRadius, int battleRadius, int regeneration, int minPlayers, int skillExperiencePerWin
    ) implements CustomPayload {
        public static final Id<UpdateBossSpawnerPayload> ID = new Id<>(Identifier.of(YggdrasilLdMod.MOD_ID, "update_boss_spawner"));
        public static final PacketCodec<PacketByteBuf, UpdateBossSpawnerPayload> CODEC = PacketCodec.of(
                UpdateBossSpawnerPayload::write, UpdateBossSpawnerPayload::new);

        public UpdateBossSpawnerPayload(PacketByteBuf buf) {
            this(
                    buf.readBlockPos(),
                    buf.readString(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readString(),
                    buf.readBlockPos(),
                    buf.readBlockPos(),
                    buf.readBlockPos(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()
            );
        }

        public void write(PacketByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeString(mobId);
            buf.writeVarInt(respawnTime);
            buf.writeVarInt(portalTime);
            buf.writeString(lootTable);
            buf.writeBlockPos(exitCoords);
            buf.writeBlockPos(enterSpawnCoords);
            buf.writeBlockPos(enterDestCoords);
            buf.writeVarInt(triggerRadius);
            buf.writeVarInt(battleRadius);
            buf.writeVarInt(regeneration);
            buf.writeVarInt(minPlayers);
            buf.writeVarInt(skillExperiencePerWin);
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record UpdateMobSpawnerPayload(
            BlockPos pos, String mobId, int respawnTime, String lootTable,
            int triggerRadius, int battleRadius, int regeneration, int skillExperience,
            int mobCount, int mobSpread, double mobHealth, double mobAttackDamage
    ) implements CustomPayload {
        public static final Id<UpdateMobSpawnerPayload> ID = new Id<>(Identifier.of(YggdrasilLdMod.MOD_ID, "update_mob_spawner"));
        public static final PacketCodec<PacketByteBuf, UpdateMobSpawnerPayload> CODEC = PacketCodec.of(
                UpdateMobSpawnerPayload::write, UpdateMobSpawnerPayload::new);

        public UpdateMobSpawnerPayload(PacketByteBuf buf) {
            this(
                    buf.readBlockPos(), buf.readString(), buf.readVarInt(), buf.readString(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readDouble(), buf.readDouble()
            );
        }

        public void write(PacketByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeString(mobId);
            buf.writeVarInt(respawnTime);
            buf.writeString(lootTable);
            buf.writeVarInt(triggerRadius);
            buf.writeVarInt(battleRadius);
            buf.writeVarInt(regeneration);
            buf.writeVarInt(skillExperience);
            buf.writeVarInt(mobCount);
            buf.writeVarInt(mobSpread);
            buf.writeDouble(mobHealth);
            buf.writeDouble(mobAttackDamage);
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }


    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(ReputationSyncPayload.ID, ReputationSyncPayload.CODEC);
    }

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(UpdateBossSpawnerPayload.ID, UpdateBossSpawnerPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateMobSpawnerPayload.ID, UpdateMobSpawnerPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateBossSpawnerPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                World world = context.player().getWorld();
                if (world.getBlockEntity(payload.pos()) instanceof BossSpawnerBlockEntity blockEntity) {
                    blockEntity.mobId = payload.mobId();
                    blockEntity.respawnTime = payload.respawnTime();
                    blockEntity.portalActiveTime = payload.portalTime();
                    blockEntity.lootTableId = payload.lootTable();
                    blockEntity.exitPortalCoords = payload.exitCoords();
                    blockEntity.enterPortalSpawnCoords = payload.enterSpawnCoords();
                    blockEntity.enterPortalDestCoords = payload.enterDestCoords();
                    blockEntity.triggerRadius = payload.triggerRadius();
                    blockEntity.battleRadius = payload.battleRadius();
                    blockEntity.regeneration = payload.regeneration();
                    blockEntity.minPlayers = payload.minPlayers();
                    blockEntity.skillExperiencePerWin = payload.skillExperiencePerWin();
                    blockEntity.markDirty();
                    world.updateListeners(payload.pos(), blockEntity.getCachedState(), blockEntity.getCachedState(), 3);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UpdateMobSpawnerPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                World world = context.player().getWorld();
                if (world.getBlockEntity(payload.pos()) instanceof MobSpawnerBlockEntity blockEntity) {
                    blockEntity.mobId = payload.mobId();
                    blockEntity.respawnTime = payload.respawnTime();
                    blockEntity.lootTableId = payload.lootTable();
                    blockEntity.triggerRadius = payload.triggerRadius();
                    blockEntity.battleRadius = payload.battleRadius();
                    blockEntity.regeneration = payload.regeneration();
                    blockEntity.skillExperiencePerWin = payload.skillExperience();
                    blockEntity.mobCount = payload.mobCount();
                    blockEntity.mobSpread = payload.mobSpread();
                    blockEntity.mobHealth = payload.mobHealth();
                    blockEntity.mobAttackDamage = payload.mobAttackDamage();
                    blockEntity.markDirty();
                    world.updateListeners(payload.pos(), blockEntity.getCachedState(), blockEntity.getCachedState(), 3);
                }
            });
        });
    }
}

