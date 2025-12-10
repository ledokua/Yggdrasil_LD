package net.ledok.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ledok.YggdrasilLdMod;
import net.ledok.block.entity.BossSpawnerBlockEntity;
import net.ledok.block.entity.MobSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ModPackets {

    public record ReputationSyncPayload(UUID playerUuid, int reputation) implements CustomPacketPayload {
        public static final Type<ReputationSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "reputation_sync"));

        // FIX: The method reference for the writer was causing a type inference error.
        // Using a lambda explicitly defines the parameter order (buffer, then payload).
        public static final StreamCodec<FriendlyByteBuf, ReputationSyncPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), ReputationSyncPayload::new);

        public ReputationSyncPayload(FriendlyByteBuf buf) {
            this(buf.readUUID(), buf.readInt());
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeUUID(playerUuid);
            buf.writeInt(reputation);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record UpdateBossSpawnerPayload(
            BlockPos pos, String mobId, int respawnTime, int portalTime, String lootTable,
            BlockPos exitCoords, BlockPos enterSpawnCoords, BlockPos enterDestCoords,
            int triggerRadius, int battleRadius, int regeneration, int minPlayers, int skillExperiencePerWin
    ) implements CustomPacketPayload {
        public static final Type<UpdateBossSpawnerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "update_boss_spawner"));

        // FIX: Used a lambda for the writer to resolve the type inference error.
        public static final StreamCodec<FriendlyByteBuf, UpdateBossSpawnerPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), UpdateBossSpawnerPayload::new);

        public UpdateBossSpawnerPayload(FriendlyByteBuf buf) {
            this(
                    buf.readBlockPos(), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(),
                    buf.readBlockPos(), buf.readBlockPos(), buf.readBlockPos(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()
            );
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeUtf(mobId);
            buf.writeVarInt(respawnTime);
            buf.writeVarInt(portalTime);
            buf.writeUtf(lootTable);
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
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record UpdateMobSpawnerPayload(
            BlockPos pos, String mobId, int respawnTime, String lootTable,
            int triggerRadius, int battleRadius, int regeneration, int skillExperience,
            int mobCount, int mobSpread, double mobHealth, double mobAttackDamage, String groupId
    ) implements CustomPacketPayload {
        public static final Type<UpdateMobSpawnerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "update_mob_spawner"));

        // FIX: Used a lambda for the writer to resolve the type inference error.
        public static final StreamCodec<FriendlyByteBuf, UpdateMobSpawnerPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), UpdateMobSpawnerPayload::new);

        public UpdateMobSpawnerPayload(FriendlyByteBuf buf) {
            this(
                    buf.readBlockPos(), buf.readUtf(), buf.readVarInt(), buf.readUtf(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readUtf()
            );
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeUtf(mobId);
            buf.writeVarInt(respawnTime);
            buf.writeUtf(lootTable);
            buf.writeVarInt(triggerRadius);
            buf.writeVarInt(battleRadius);
            buf.writeVarInt(regeneration);
            buf.writeVarInt(skillExperience);
            buf.writeVarInt(mobCount);
            buf.writeVarInt(mobSpread);
            buf.writeDouble(mobHealth);
            buf.writeDouble(mobAttackDamage);
            buf.writeUtf(groupId);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(ReputationSyncPayload.TYPE, ReputationSyncPayload.STREAM_CODEC);
    }

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(UpdateBossSpawnerPayload.TYPE, UpdateBossSpawnerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateMobSpawnerPayload.TYPE, UpdateMobSpawnerPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateBossSpawnerPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                Level world = context.player().level();
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
                    blockEntity.setChanged();
                    world.sendBlockUpdated(payload.pos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UpdateMobSpawnerPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                Level world = context.player().level();
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
                    blockEntity.groupId = payload.groupId();
                    blockEntity.setChanged();
                    world.sendBlockUpdated(payload.pos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                }
            });
        });
    }
}

