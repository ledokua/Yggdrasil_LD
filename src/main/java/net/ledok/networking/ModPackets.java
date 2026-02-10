package net.ledok.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ledok.YggdrasilLdMod;
import net.ledok.registry.LootBoxDefinition;
import net.ledok.util.AttributeData;
import net.ledok.util.AttributeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.UUID;

public class ModPackets {

    public record ReputationSyncPayload(UUID playerUuid, int reputation) implements CustomPacketPayload {
        public static final Type<ReputationSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "reputation_sync"));

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

    public record SyncLootBoxesPayload(List<LootBoxDefinition> definitions) implements CustomPacketPayload {
        public static final Type<SyncLootBoxesPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "sync_loot_boxes"));

        public static final StreamCodec<FriendlyByteBuf, SyncLootBoxesPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), SyncLootBoxesPayload::new);

        public SyncLootBoxesPayload(FriendlyByteBuf buf) {
            this(buf.readList(b -> new LootBoxDefinition(
                    b.readUtf(),
                    b.readUtf(),
                    b.readUtf(),
                    b.readInt(),
                    b.readBoolean()
            )));
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeCollection(definitions, (b, def) -> {
                b.writeUtf(def.id());
                b.writeUtf(def.name());
                b.writeUtf(def.lootTableId());
                b.writeInt(def.color());
                b.writeBoolean(def.glow());
            });
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
            int mobCount, int mobSpread, String groupId
    ) implements CustomPacketPayload {
        public static final Type<UpdateMobSpawnerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "update_mob_spawner"));

        public static final StreamCodec<FriendlyByteBuf, UpdateMobSpawnerPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), UpdateMobSpawnerPayload::new);

        public UpdateMobSpawnerPayload(FriendlyByteBuf buf) {
            this(
                    buf.readBlockPos(), buf.readUtf(), buf.readVarInt(), buf.readUtf(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readUtf()
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
            buf.writeUtf(groupId);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record UpdateAttributesPayload(
            BlockPos pos, List<AttributeData> attributes
    ) implements CustomPacketPayload {
        public static final Type<UpdateAttributesPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "update_attributes"));

        public static final StreamCodec<FriendlyByteBuf, UpdateAttributesPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), UpdateAttributesPayload::new);

        public UpdateAttributesPayload(FriendlyByteBuf buf) {
            this(
                    buf.readBlockPos(),
                    buf.readList(b -> new AttributeData(b.readUtf(), b.readDouble()))
            );
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeCollection(attributes, (b, attr) -> {
                b.writeUtf(attr.id());
                b.writeDouble(attr.value());
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(ReputationSyncPayload.TYPE, ReputationSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncLootBoxesPayload.TYPE, SyncLootBoxesPayload.STREAM_CODEC);
    }

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(UpdateBossSpawnerPayload.TYPE, UpdateBossSpawnerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateMobSpawnerPayload.TYPE, UpdateMobSpawnerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateAttributesPayload.TYPE, UpdateAttributesPayload.STREAM_CODEC);


        ServerPlayNetworking.registerGlobalReceiver(UpdateAttributesPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                Level world = context.player().level();
                BlockEntity be = world.getBlockEntity(payload.pos());
                if (be instanceof AttributeProvider provider) {
                    provider.setAttributes(payload.attributes());
                    be.setChanged();
                    world.sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 3);
                }
            });
        });
    }
}
