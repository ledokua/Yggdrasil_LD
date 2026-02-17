package net.ledok.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.ledok.YggdrasilLdMod;
import net.ledok.block.LiquidManaBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockRegistry {
    public static final Block LIQUID_MANA_BLOCK = Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "liquid_mana_block"), new LiquidManaBlock(FluidRegistry.STILL_LIQUID_MANA, FabricBlockSettings.copyOf(Blocks.WATER).noCollision().strength(100.0F).noLootTable()));

    public static void initialize() {
    }
}
