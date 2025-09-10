package net.ledok.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.ledok.Yggdrasil_ld;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModConfigs {

    // Список ідентифікаторів вимірів, де буст елітр заборонений
    public List<String> elytra_boost_disabled_dimensions = new ArrayList<>(Arrays.asList(
            "minecraft:the_end",
            "minecraft:the_nether"
    ));

    // Нова опція для відсотка випадіння предметів
    public double keep_inventory_drop_percentage = 50.0;


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), Yggdrasil_ld.MOD_ID + ".json");

    // Метод для завантаження конфігурації
    public static ModConfigs load() {
        ModConfigs config;
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, ModConfigs.class);
                if (config == null) {
                    config = new ModConfigs();
                }
            } catch (IOException e) {
                Yggdrasil_ld.LOGGER.error("Failed to load config, using defaults.", e);
                config = new ModConfigs();
            }
        } else {
            config = new ModConfigs();
        }
        config.save(); // Зберігаємо, щоб створити файл з дефолтними значеннями, якщо його не було
        return config;
    }

    // Метод для збереження конфігурації
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            Yggdrasil_ld.LOGGER.error("Failed to save config.", e);
        }
    }
}