package net.ledok.screen;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;

/**
 * A simple record to hold the BlockPos of the Boss Spawner.
 * This is required for the modern ExtendedScreenHandlerFactory.
 */
public record BossSpawnerData(BlockPos blockPos) {
    public static final PacketCodec<PacketByteBuf, BossSpawnerData> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBlockPos(value.blockPos),
            (buf) -> new BossSpawnerData(buf.readBlockPos())
    );
}
