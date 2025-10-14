package net.ledok.screen;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;

public record MobSpawnerData(BlockPos blockPos) {
    public static final PacketCodec<PacketByteBuf, MobSpawnerData> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBlockPos(value.blockPos),
            (buf) -> new MobSpawnerData(buf.readBlockPos())
    );
}
