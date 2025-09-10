package net.ledok;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.ledok.Items.Items;
import net.ledok.config.ModConfigs;
import net.ledok.event.ElytraBoostDisabler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Yggdrasil_ld implements ModInitializer {
    public static final String MOD_ID = "yggdrasil_ld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Додаємо статичну змінну для конфігурації
    public static ModConfigs CONFIG;

    @Override
    public void onInitialize() {
        LOGGER.info("Yggdrasil LD has been initialized!");

        // Ініціалізуємо предмети
        Items.initialize();

        // Завантажуємо конфігурацію при старті гри
        CONFIG = ModConfigs.load();

        // Реєструємо наш обробник подій
        UseItemCallback.EVENT.register(new ElytraBoostDisabler());
    }
}