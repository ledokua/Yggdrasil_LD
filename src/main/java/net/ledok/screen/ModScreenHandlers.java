package net.ledok.screen;

// MOJANG MAPPINGS: Update all imports to their new Mojang-mapped packages.
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.ledok.YggdrasilLdMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;


public class ModScreenHandlers {

    // MOJANG MAPPINGS: ScreenHandlerType is now MenuType.
    // FIX: Use ResourceLocation.parse() instead of the private constructor.
    public static final MenuType<BossSpawnerScreenHandler> BOSS_SPAWNER_SCREEN_HANDLER =
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.parse(YggdrasilLdMod.MOD_ID + ":boss_spawner"),
                    new ExtendedScreenHandlerType<>(BossSpawnerScreenHandler::new, BossSpawnerData.CODEC));

    public static final MenuType<MobSpawnerScreenHandler> MOB_SPAWNER_SCREEN_HANDLER =
            Registry.register(BuiltInRegistries.MENU, ResourceLocation.parse(YggdrasilLdMod.MOD_ID + ":mob_spawner"),
                    new ExtendedScreenHandlerType<>(MobSpawnerScreenHandler::new, MobSpawnerData.CODEC));

    public static void initialize() {
    }
}