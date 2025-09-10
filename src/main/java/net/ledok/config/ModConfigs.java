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

    // ... існуючі налаштування ...
    public List<String> elytra_boost_disabled_dimensions = new ArrayList<>(Arrays.asList("minecraft:the_end", "minecraft:the_nether"));
    public double keep_inventory_drop_percentage = 50.0;
    public boolean reputation_affects_drops = true;

    // --- ОНОВЛЕНІ НАЛАШТУВАННЯ ВТРАТИ РЕПУТАЦІЇ ---
    public double reputation_death_penalty_negative_percentage = 5.0;
    public double reputation_death_penalty_positive_percentage = 2.0;

    public int reputation_pvp_kill_penalty = 100;
    public int reputation_pvp_kill_victim_bonus_threshold = -500;
    public int reputation_pvp_kill_victim_bonus = 20;
    public int reputation_penalty_threshold = -750;
    public int reputation_penalty_item_count = 3;
    public boolean predatory_kill_enabled = true;
    public int predatory_kill_victim_positive_rep_threshold = 100;
    public int predatory_kill_inventory_drop_rep_step = 1500;
    public int predatory_kill_equipment_drop_rep_step = 10000;
    public int predatory_kill_equipment_drop_max = 5;
    public boolean reputation_stealing_enabled = true;
    public int reputation_stealing_threshold = -1000;
    public double reputation_stealing_transfer_percentage = 40.0;
    public double reputation_stealing_vanish_percentage = 10.0;
    public boolean bounty_hunter_kill_enabled = true;
    public int bounty_hunter_attacker_positive_rep_threshold = 100;
    public int bounty_hunter_victim_negative_rep_threshold = -1000;
    public double bounty_hunter_victim_rep_loss_percentage = 50.0;
    public int bounty_hunter_bonus_rep_per_step = 20;
    public int bounty_hunter_bonus_rep_step = 1000;


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), Yggdrasil_ld.MOD_ID + ".json");

    public static ModConfigs load() {
        ModConfigs config;
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, ModConfigs.class);
                if (config == null) { config = new ModConfigs(); }
            } catch (IOException e) {
                Yggdrasil_ld.LOGGER.error("Failed to load config, using defaults.", e);
                config = new ModConfigs();
            }
        } else {
            config = new ModConfigs();
        }
        config.save();
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            Yggdrasil_ld.LOGGER.error("Failed to save config.", e);
        }
    }
}

