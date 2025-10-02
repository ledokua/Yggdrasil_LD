package net.ledok.block;

import net.ledok.Yggdrasil_ld;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

// --- Blocks register ---
public class ModBlocks {

    public static final Block BOSS_SPAWNER_BLOCK = registerBlock("boss_spawner",
            new BossSpawnerBlock(AbstractBlock.Settings.copy(Blocks.SPAWNER)
                    .strength(-1.0f, 3600000.0f)));

    public static final Block EXIT_PORTAL_BLOCK = registerBlock("exit_portal",
            new ExitPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL).sounds(BlockSoundGroup.GLASS).noCollision()));

    public static final Block ENTER_PORTAL_BLOCK = registerBlock("enter_portal",
            new EnterPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL).sounds(BlockSoundGroup.GLASS).noCollision()));


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(Yggdrasil_ld.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(Yggdrasil_ld.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void initialize() { }
}

