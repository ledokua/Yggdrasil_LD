package net.ledok.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.ledok.YggdrasilLdMod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModConfigs {
    // --- Puffish Skills Main skill tree NOT USED ---
    public String puffish_skills_tree_id = "puffish_skills:minestar";

    // --- Runtime toggle for the partial inventory save feature ---
    public transient boolean partial_inventory_save_enabled = true;
    // --- Master switch for all reputation changes ---
    public transient boolean reputation_change_enabled = true;

    // --- Passive Reputation Gain Settings ---
    public boolean passive_reputation_enabled = true;
    public int passive_reputation_interval_minutes = 10;
    public int passive_reputation_amount = 1;

    // --- PRIME ---
    public boolean prime_role_sync_enabled= true;
    // --- LEADERBOARD SIZE ---
    public int leaderboard_size= 10;

    // --- ANTI-ABUSE SETTING ---
    public int pvp_cooldown_ticks = 1200; // Time in ticks

    // --- ELYTRA BOOST BLACKLISTED DIMENSIONS
    public List<String> elytra_boost_disabled_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld", "minecraft:the_nether"));

    // --- REPUTATION AND PARTIAL INVENTORY SAVE
    public double keep_inventory_drop_percentage = 50.0;
    public boolean reputation_affects_drops = true;
    public double reputation_death_penalty_negative_percentage = 5.0;
    public double reputation_death_penalty_positive_percentage = 2.0;
    public int reputation_penalty_threshold = -750;
    public int reputation_penalty_item_count = 4;
    public boolean predatory_kill_enabled = true;
    public int predatory_kill_victim_positive_rep_threshold = 100;
    public int predatory_kill_inventory_drop_rep_step = 1500;
    public int predatory_kill_equipment_drop_rep_step = 6000;
    public int predatory_kill_equipment_drop_max = 5;


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), YggdrasilLdMod.MOD_ID + ".json");

    public static ModConfigs load() {
        ModConfigs config;
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, ModConfigs.class);
                if (config == null) { config = new ModConfigs(); }
            } catch (IOException e) {
                YggdrasilLdMod.LOGGER.error("Failed to load config, using defaults.", e);
                config = new ModConfigs();
            }
        } else {
            config = new ModConfigs();
        }
        config.reputation_change_enabled = true;
        config.partial_inventory_save_enabled = true;
        config.save();
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            YggdrasilLdMod.LOGGER.error("Failed to save config.", e);
        }
    }
}
