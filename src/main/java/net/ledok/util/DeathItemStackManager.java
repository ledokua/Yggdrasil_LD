package net.ledok.util;

import net.minecraft.item.ItemStack;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathItemStackManager {

    // Внутрішній клас для зберігання повної інформації про Trinket
    public static class TrinketInfo {
        public final String group;
        public final String name;
        public final int index;
        public final ItemStack stack;

        public TrinketInfo(String group, String name, int index, ItemStack stack) {
            this.group = group;
            this.name = name;
            this.index = index;
            this.stack = stack;
        }
    }

    // Клас для зберігання всіх предметів гравця
    public static class KeptItems {
        public final Map<Integer, ItemStack> vanillaItems;
        public final List<TrinketInfo> trinketItems;

        public KeptItems(Map<Integer, ItemStack> vanillaItems, List<TrinketInfo> trinketItems) {
            this.vanillaItems = vanillaItems;
            this.trinketItems = trinketItems;
        }
    }

    private static final Map<UUID, KeptItems> KEPT_ITEMS_STORE = new ConcurrentHashMap<>();

    public static void keepItems(UUID playerUuid, Map<Integer, ItemStack> vanillaItems, List<TrinketInfo> trinketItems) {
        KEPT_ITEMS_STORE.put(playerUuid, new KeptItems(vanillaItems, trinketItems));
    }

    public static KeptItems restoreItems(UUID playerUuid) {
        return KEPT_ITEMS_STORE.remove(playerUuid);
    }
}

