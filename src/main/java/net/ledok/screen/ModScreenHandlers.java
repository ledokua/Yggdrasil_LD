package net.ledok.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.ledok.YggdrasilLdMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {

    // --- Constructor for ExtendedScreenHandlerType ---
    public static final ScreenHandlerType<BossSpawnerScreenHandler> BOSS_SPAWNER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(YggdrasilLdMod.MOD_ID, "boss_spawner"),
                    new ExtendedScreenHandlerType<>(BossSpawnerScreenHandler::new, BossSpawnerData.CODEC));

    public static final ScreenHandlerType<MobSpawnerScreenHandler> MOB_SPAWNER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(YggdrasilLdMod.MOD_ID, "mob_spawner"),
                    new ExtendedScreenHandlerType<>(MobSpawnerScreenHandler::new, MobSpawnerData.CODEC));

    public static void initialize() {
    }
}

