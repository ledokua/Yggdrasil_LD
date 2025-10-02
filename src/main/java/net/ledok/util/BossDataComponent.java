package net.ledok.util;

import com.mojang.serialization.Codec;
import net.ledok.YggdrasilLdMod;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public record BossDataComponent(int bossLevel) {
    public static final Codec<BossDataComponent> CODEC = Codec.INT.xmap(BossDataComponent::new, BossDataComponent::bossLevel);

    // --- NOT USED FOR NOW ---
    public static final ComponentType<BossDataComponent> BOSS_DATA = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(YggdrasilLdMod.MOD_ID, "boss_data"),
            ComponentType.<BossDataComponent>builder().codec(CODEC).packetCodec(PacketCodecs.codec(CODEC)).build()
    );

    public static void initialize() {
    }
}

