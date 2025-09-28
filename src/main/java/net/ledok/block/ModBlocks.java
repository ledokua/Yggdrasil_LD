package net.ledok.block;

import net.ledok.Yggdrasil_ld;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.block.AbstractBlock;

public class ModBlocks {

    // --- FIX: Added .strength(-1.0f) to make the block indestructible ---
    public static final Block BOSS_SPAWNER_BLOCK = registerBlock("boss_spawner",
            new BossSpawnerBlock(AbstractBlock.Settings.copy(Blocks.SPAWNER).strength(-1.0f)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(Yggdrasil_ld.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(Yggdrasil_ld.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void initialize() {
        // This method is called to ensure the blocks are registered.
    }
}

