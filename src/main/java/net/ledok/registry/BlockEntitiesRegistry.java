package net.ledok.registry;

import net.ledok.YggdrasilLdMod;
import net.ledok.block.entity.BossSpawnerBlockEntity;
import net.ledok.block.entity.EnterPortalBlockEntity;
import net.ledok.block.entity.ExitPortalBlockEntity;
import net.ledok.block.entity.MobSpawnerBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntitiesRegistry {
    // --- Block entities register ---
    public static final BlockEntityType<BossSpawnerBlockEntity> BOSS_SPAWNER_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.parse(YggdrasilLdMod.MOD_ID + ":boss_spawner_be"),
                    BlockEntityType.Builder.of(BossSpawnerBlockEntity::new, BlockRegistry.BOSS_SPAWNER_BLOCK).build(null));

    public static final BlockEntityType<ExitPortalBlockEntity> EXIT_PORTAL_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.parse(YggdrasilLdMod.MOD_ID + ":exit_portal_be"),
                    BlockEntityType.Builder.of(ExitPortalBlockEntity::new, BlockRegistry.EXIT_PORTAL_BLOCK).build(null));

    public static final BlockEntityType<EnterPortalBlockEntity> ENTER_PORTAL_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.parse(YggdrasilLdMod.MOD_ID + ":enter_portal_be"),
                    BlockEntityType.Builder.of(EnterPortalBlockEntity::new, BlockRegistry.ENTER_PORTAL_BLOCK).build(null));

    public static final BlockEntityType<MobSpawnerBlockEntity> MOB_SPAWNER_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.parse(YggdrasilLdMod.MOD_ID + ":mob_spawner_be"),
                    BlockEntityType.Builder.of(MobSpawnerBlockEntity::new, BlockRegistry.MOB_SPAWNER_BLOCK).build(null));


    public static void initialize() {
    }
}
