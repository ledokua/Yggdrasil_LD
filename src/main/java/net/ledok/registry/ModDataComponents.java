package net.ledok.registry;

import com.mojang.serialization.Codec;
import net.ledok.YggdrasilLdMod;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

public class ModDataComponents {
    public static DataComponentType<String> LOOT_BOX_ID;

    public static void initialize() {
        LOOT_BOX_ID = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, 
                ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "loot_box_id"), 
                DataComponentType.<String>builder()
                        .persistent(Codec.STRING)
                        .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                        .build());
    }
}
