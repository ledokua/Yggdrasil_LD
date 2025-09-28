package net.ledok.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ledok.Yggdrasil_ld;
import net.ledok.block.entity.BossSpawnerBlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class ModPackets {

    // --- S2C Reputation Sync Packet ---
    public record ReputationSyncPayload(UUID playerUuid, int reputation) implements CustomPayload {
        public static final CustomPayload.Id<ReputationSyncPayload> ID = new CustomPayload.Id<>(Identifier.of(Yggdrasil_ld.MOD_ID, "reputation_sync"));
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

    // --- C2S Boss Spawner Update Packet ---
    public record UpdateBossSpawnerPayload(BlockPos pos, String mobId, int respawnTime, int bossLevel, int portalTime, String lootTable, BlockPos exitCoords, int triggerRadius, int battleRadius, int regeneration) implements CustomPayload {
        public static final CustomPayload.Id<UpdateBossSpawnerPayload> ID = new CustomPayload.Id<>(Identifier.of(Yggdrasil_ld.MOD_ID, "update_boss_spawner"));
        public static final PacketCodec<PacketByteBuf, UpdateBossSpawnerPayload> CODEC = PacketCodec.of(UpdateBossSpawnerPayload::write, UpdateBossSpawnerPayload::new);

        public UpdateBossSpawnerPayload(PacketByteBuf buf) {
            this(buf.readBlockPos(), buf.readString(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readString(), buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readInt());
        }

        public void write(PacketByteBuf buf) {
            buf.writeBlockPos(pos);
            buf.writeString(mobId);
            buf.writeInt(respawnTime);
            buf.writeInt(bossLevel);
            buf.writeInt(portalTime);
            buf.writeString(lootTable);
            buf.writeBlockPos(exitCoords);
            buf.writeInt(triggerRadius);
            buf.writeInt(battleRadius);
            buf.writeInt(regeneration);
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }


    // --- Packet Registration ---
    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(ReputationSyncPayload.ID, ReputationSyncPayload.CODEC);
    }

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(UpdateBossSpawnerPayload.ID, UpdateBossSpawnerPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateBossSpawnerPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                World world = context.player().getWorld();
                if (world.getBlockEntity(payload.pos()) instanceof BossSpawnerBlockEntity blockEntity) {
                    blockEntity.mobId = payload.mobId();
                    blockEntity.respawnTime = payload.respawnTime();
                    blockEntity.bossLevel = payload.bossLevel();
                    blockEntity.portalActiveTime = payload.portalTime();
                    blockEntity.lootTableId = payload.lootTable();
                    blockEntity.exitPortalCoords = payload.exitCoords();
                    blockEntity.triggerRadius = payload.triggerRadius();
                    blockEntity.battleRadius = payload.battleRadius();
                    blockEntity.regeneration = payload.regeneration();
                    blockEntity.markDirty(); // Saves the data to the world file

                    // --- FIX: This line is crucial. ---
                    // It tells the server to send an update packet to all nearby clients
                    // to sync the block entity's new data.
                    world.updateListeners(payload.pos(), blockEntity.getCachedState(), blockEntity.getCachedState(), 3);
                }
            });
        });
    }
}

