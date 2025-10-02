package net.ledok.block.entity;

import net.ledok.Yggdrasil_ld;
import net.ledok.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    // --- FIX: Switched from the deprecated FabricBlockEntityTypeBuilder to the standard one ---
    public static final BlockEntityType<BossSpawnerBlockEntity> BOSS_SPAWNER_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(Yggdrasil_ld.MOD_ID, "boss_spawner_be"),
                    BlockEntityType.Builder.create(BossSpawnerBlockEntity::new, ModBlocks.BOSS_SPAWNER_BLOCK).build());

    public static final BlockEntityType<ExitPortalBlockEntity> EXIT_PORTAL_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(Yggdrasil_ld.MOD_ID, "exit_portal_be"),
                    BlockEntityType.Builder.create(ExitPortalBlockEntity::new, ModBlocks.EXIT_PORTAL_BLOCK).build());

    public static final BlockEntityType<EnterPortalBlockEntity> ENTER_PORTAL_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(Yggdrasil_ld.MOD_ID, "enter_portal_be"),
                    BlockEntityType.Builder.create(EnterPortalBlockEntity::new, ModBlocks.ENTER_PORTAL_BLOCK).build());


    public static void initialize() {
        // This method is called to ensure the block entities are registered.
    }
}

