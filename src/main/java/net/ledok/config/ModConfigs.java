package net.ledok.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModConfigs {
    // --- Puffish Skills skill trees used by reset scroll ---
    public List<String> puffish_skills_tree_ids = new ArrayList<>(Collections.singletonList("puffish_skills:minestar"));
    // --- Puffish Skills tree used by XP potions/items ---
    public String puffish_skills_xp_tree_id = "puffish_skills:minestar";

    // --- Runtime toggle for the partial inventory save feature ---
    public transient boolean partial_inventory_save_enabled = true;

    // --- PRIME ---
    public boolean prime_role_sync_enabled = true;

    // --- ELYTRA BOOST BLACKLISTED DIMENSIONS
    public List<String> elytra_boost_disabled_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld", "minecraft:the_nether"));
    public boolean elytra_armor_threshold_enabled = true;
    public int elytra_armor_threshold = 20;

    // --- PARTIAL INVENTORY SAVE
    public double keep_inventory_drop_percentage = 50.0;


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), YggdrasilLdMod.MOD_ID + ".json");

    public static ModConfigs load() {
        ModConfigs config;
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                config = GSON.fromJson(root, ModConfigs.class);
                if (config == null) { config = new ModConfigs(); }
                boolean hasExplicitXpTreeId = root.has("puffish_skills_xp_tree_id");
                config.migrateLegacyPuffishSkillsTreeIds(root);
                config.normalizePuffishSkillsTreeIds();
                config.normalizePuffishSkillsXpTreeId(!hasExplicitXpTreeId);
                if (config.elytra_boost_disabled_dimensions == null) {
                    config.elytra_boost_disabled_dimensions = new ArrayList<>(Arrays.asList("minecraft:overworld", "minecraft:the_nether"));
                }
                try {
                    boolean checkEnabled = config.elytra_armor_threshold_enabled;
                    int checkThreshold = config.elytra_armor_threshold;
                } catch (NullPointerException | NoSuchFieldError e) {
                    config.elytra_armor_threshold_enabled = true;
                    config.elytra_armor_threshold = 20;
                }

            }catch (IOException e) {
                YggdrasilLdMod.LOGGER.error("Failed to load config, using defaults.", e);
                config = new ModConfigs();
            }
        } else {
            config = new ModConfigs();
        }
        config.normalizePuffishSkillsTreeIds();
        config.normalizePuffishSkillsXpTreeId(false);
        config.partial_inventory_save_enabled = true;
        config.save();
        return config;
    }

    public List<String> getPuffishSkillsTreeIds() {
        return puffish_skills_tree_ids;
    }

    public String getPrimaryPuffishSkillsTreeId() {
        return puffish_skills_tree_ids.getFirst();
    }

    public String getPuffishSkillsXpTreeId() {
        return puffish_skills_xp_tree_id;
    }

    private void migrateLegacyPuffishSkillsTreeIds(JsonObject root) {
        if (root == null || !root.has("puffish_skills_tree_id")) {
            return;
        }

        JsonElement legacyTreeId = root.get("puffish_skills_tree_id");
        if (legacyTreeId.isJsonPrimitive() && legacyTreeId.getAsJsonPrimitive().isString()) {
            puffish_skills_tree_ids = new ArrayList<>(Collections.singletonList(legacyTreeId.getAsString()));
        }
    }

    private void normalizePuffishSkillsTreeIds() {
        if (puffish_skills_tree_ids == null) {
            puffish_skills_tree_ids = new ArrayList<>();
        }

        puffish_skills_tree_ids = puffish_skills_tree_ids.stream()
                .filter(treeId -> treeId != null && !treeId.isBlank())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        if (puffish_skills_tree_ids.isEmpty()) {
            puffish_skills_tree_ids.add("puffish_skills:minestar");
        }
    }

    private void normalizePuffishSkillsXpTreeId(boolean preferPrimaryTree) {
        if (puffish_skills_xp_tree_id == null || puffish_skills_xp_tree_id.isBlank()) {
            puffish_skills_xp_tree_id = preferPrimaryTree
                    ? getPrimaryPuffishSkillsTreeId()
                    : "puffish_skills:minestar";
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            YggdrasilLdMod.LOGGER.error("Failed to save config.", e);
        }
    }
}
