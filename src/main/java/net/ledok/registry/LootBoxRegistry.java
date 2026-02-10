package net.ledok.registry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.ledok.Items.LootBoxItem;
import net.ledok.YggdrasilLdMod;
import net.ledok.networking.ModPackets;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LootBoxRegistry {

    public static final Item LOOT_BOX_ITEM = new LootBoxItem(new Item.Properties().stacksTo(16));
    private static final Map<String, LootBoxDefinition> DEFINITIONS = new ConcurrentHashMap<>();
    private static final String LOOTBOX_PATH = "lootboxes";

    public static void initialize() {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "loot_box"), LOOT_BOX_ITEM);
    }

    public static void initializeServer() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, LOOTBOX_PATH);
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                synchronized (DEFINITIONS) {
                    YggdrasilLdMod.LOGGER.info("Reloading loot box definitions on server...");
                    DEFINITIONS.clear();
                    Gson gson = new Gson();

                    var resources = manager.listResources(LOOTBOX_PATH, location -> location.getNamespace().equals(YggdrasilLdMod.MOD_ID) && location.getPath().endsWith(".json"));
                    YggdrasilLdMod.LOGGER.info("Found " + resources.size() + " loot box definition files in path '" + LOOTBOX_PATH + "'.");

                    resources.forEach((location, resource) -> {
                        try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                            JsonObject json = gson.fromJson(reader, JsonObject.class);
                            String id = json.get("id").getAsString();
                            
                            // Parse color and ensure alpha is 255 (opaque)
                            int color = 0xFFFFFFFF; // Default white opaque
                            if (json.has("color")) {
                                String colorStr = json.get("color").getAsString().replace("#", "");
                                try {
                                    // Force alpha to FF
                                    color = 0xFF000000 | Integer.parseInt(colorStr, 16);
                                } catch (NumberFormatException e) {
                                    YggdrasilLdMod.LOGGER.error("Invalid color format for loot box " + id + ": " + colorStr);
                                }
                            }
                            
                            boolean glow = json.has("glow") && json.get("glow").getAsBoolean();

                            DEFINITIONS.put(id, new LootBoxDefinition(
                                    id,
                                    json.get("name").getAsString(),
                                    json.get("loot_table_id").getAsString(),
                                    color,
                                    glow
                            ));
                            YggdrasilLdMod.LOGGER.info("Successfully loaded loot box: " + id);
                        } catch (Exception e) {
                            YggdrasilLdMod.LOGGER.error("Failed to load loot box definition from: " + location, e);
                        }
                    });
                }
            }
        });
    }
    
    public static void syncToClient(ServerPlayer player) {
        ServerPlayNetworking.send(player, new ModPackets.SyncLootBoxesPayload(new ArrayList<>(DEFINITIONS.values())));
    }
    
    public static void setDefinitions(List<LootBoxDefinition> definitions) {
        synchronized (DEFINITIONS) {
            DEFINITIONS.clear();
            for (LootBoxDefinition def : definitions) {
                DEFINITIONS.put(def.id(), def);
            }
            YggdrasilLdMod.LOGGER.info("Received " + definitions.size() + " loot box definitions from server.");
        }
    }
    
    public static LootBoxDefinition getDefinition(String id) {
        return DEFINITIONS.get(id);
    }

    public static List<ItemStack> getAllLootBoxes() {
        List<ItemStack> stacks = new ArrayList<>();
        synchronized (DEFINITIONS) {
            for (LootBoxDefinition def : DEFINITIONS.values()) {
                ItemStack stack = new ItemStack(LOOT_BOX_ITEM);
                stack.set(ModDataComponents.LOOT_BOX_ID, def.id());
                stack.set(DataComponents.CUSTOM_NAME, Component.translatable(def.name()));
                stacks.add(stack);
            }
        }
        return stacks;
    }
    
    public static List<LootBoxDefinition> getDefinitions() {
        return new ArrayList<>(DEFINITIONS.values());
    }
}
