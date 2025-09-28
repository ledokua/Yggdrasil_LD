package net.ledok.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.ledok.Yggdrasil_ld;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {

    // --- FIX: Use the correct modern constructor for ExtendedScreenHandlerType ---
    public static final ScreenHandlerType<BossSpawnerScreenHandler> BOSS_SPAWNER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(Yggdrasil_ld.MOD_ID, "boss_spawner"),
                    new ExtendedScreenHandlerType<>(BossSpawnerScreenHandler::new, BossSpawnerData.CODEC));


    public static void initialize() {
        // This method is called to ensure the screen handlers are registered.
    }
}

