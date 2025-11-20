package net.ledok.registry;

import net.ledok.Items.DungeonLeaveItem;
import net.ledok.Items.PercentageHealItem;
import net.ledok.Items.RandomDripstoneItem;
import net.ledok.Items.SkillResetItem;
import net.ledok.YggdrasilLdMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemRegistry {
    public static final Item ICON = ItemInit.register(
            new Item(new Item.Properties()) {
                @Override
                public boolean isFoil(ItemStack stack) {
                    return true; // This makes the item have the enchantment glint
                }
            },
            "icon"
    );
    public static final Item RESET_SCROLL = ItemInit.register(
            new SkillResetItem(new Item.Properties().stacksTo(16)),
            "reset_scroll"
    );
    public static final Item ESCAPE_SCROLL = ItemInit.register(
            new DungeonLeaveItem(new Item.Properties().stacksTo(1)),
            "escape_scroll"
    );
    public static final Item DRIPSTONE_SCROLL = ItemInit.register(
            new RandomDripstoneItem(new Item.Properties().stacksTo(1)),
            "dripstone_scroll"
    );
    public static final Item HEALING_POTION_1 = ItemInit.register(
            new PercentageHealItem(new Item.Properties().stacksTo(8), 0.1f, 20, 120),
            "healing_potion_1"
    );
    public static final Item HEALING_POTION_2 = ItemInit.register(
            new PercentageHealItem(new Item.Properties().stacksTo(16), 0.25f, 40, 900),
            "healing_potion_2"
    );
    public static final Item HEALING_POTION_3 = ItemInit.register(
            new PercentageHealItem(new Item.Properties().stacksTo(16), 0.35f, 35, 1200),
            "healing_potion_3"
    );
    public static final Item HEALING_POTION_4 = ItemInit.register(
            new PercentageHealItem(new Item.Properties().stacksTo(16), 0.5f, 30, 1600),
            "healing_potion_4"
    );
    public static final Item HEALING_POTION_5 = ItemInit.register(
            new PercentageHealItem(new Item.Properties().stacksTo(16), 0.75f, 25, 3400),
            "healing_potion_5"
    );
    public static final Item HEALING_POTION_6 = ItemInit.register(
            // healPercentage: 100%, useTimeTicks: 0.25s, cooldownTicks: 10m
            new PercentageHealItem(new Item.Properties().stacksTo(1), 1f, 5, 12000),
            "healing_potion_6"
    );

    public class ItemInit {
        public static Item register(Item item, String id) {
            // Create the ResourceLocation for the item using the modern method.
            ResourceLocation itemID = ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, id);

            // Register the item to the built-in registry for items.
            Item registeredItem = Registry.register(BuiltInRegistries.ITEM, itemID, item);

            // Return the registered item.
            return registeredItem;
        }
    }

    public static void initialize() {
    }
}
