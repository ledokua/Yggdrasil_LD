package net.ledok.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ledok.YggdrasilLdMod;
import net.ledok.block.entity.BossSpawnerBlockEntity;
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
        public ReputationSyncPayload(PacketByteBuf buf) { this(buf.readUuid(), buf.readInt()); }
        public void write(PacketByteBuf buf) { buf.writeUuid(playerUuid); buf.writeInt(reputation); }
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    // --- C2S Boss Spawner Update Packet ---
    public record UpdateBossSpawnerPayload(
            BlockPos pos, String mobId, int respawnTime, int portalTime, String lootTable,
            BlockPos exitCoords, BlockPos enterSpawnCoords, BlockPos enterDestCoords, // --- NEW FIELDS ---
            int triggerRadius, int battleRadius, int regeneration
    ) implements CustomPayload {
        public static final Id<UpdateBossSpawnerPayload> ID = new Id<>(Identifier.of(YggdrasilLdMod.MOD_ID, "update_boss_spawner"));
        public static final PacketCodec<PacketByteBuf, UpdateBossSpawnerPayload> CODEC = PacketCodec.of(UpdateBossSpawnerPayload::write, UpdateBossSpawnerPayload::new);

        public UpdateBossSpawnerPayload(PacketByteBuf buf) {
            this(
                    buf.readBlockPos(), buf.readString(), buf.readInt(), buf.readInt(), buf.readString(),
                    buf.readBlockPos(), buf.readBlockPos(), buf.readBlockPos(), // --- NEW ---
                    buf.readInt(), buf.readInt(), buf.readInt()
            );
        }

        public void write(PacketByteBuf buf) {
            buf.writeBlockPos(pos); buf.writeString(mobId); buf.writeInt(respawnTime);
            buf.writeInt(portalTime); buf.writeString(lootTable); buf.writeBlockPos(exitCoords);
            // --- NEW ---
            buf.writeBlockPos(enterSpawnCoords); buf.writeBlockPos(enterDestCoords);
            buf.writeInt(triggerRadius); buf.writeInt(battleRadius); buf.writeInt(regeneration);
        }

        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

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
                    blockEntity.portalActiveTime = payload.portalTime();
                    blockEntity.lootTableId = payload.lootTable();
                    blockEntity.exitPortalCoords = payload.exitCoords();
                    // --- NEW: Update Enter Portal fields on the server ---
                    blockEntity.enterPortalSpawnCoords = payload.enterSpawnCoords();
                    blockEntity.enterPortalDestCoords = payload.enterDestCoords();
                    blockEntity.triggerRadius = payload.triggerRadius();
                    blockEntity.battleRadius = payload.battleRadius();
                    blockEntity.regeneration = payload.regeneration();
                    blockEntity.markDirty();
                    world.updateListeners(payload.pos(), blockEntity.getCachedState(), blockEntity.getCachedState(), 3);
                }
            });
        });
    }
}

