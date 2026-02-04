package net.ledok.registry;

import net.ledok.Items.*;
import net.ledok.YggdrasilLdMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
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

    public static final Item ANCIENT_ICE_SHARD = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "ancient_ice_shard"
    );

    public static final Item NETHERITE_BOTTLE = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "netherite_bottle"
    );

    public static final Item BAT_WING = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "bat_wing"
    );

    public static final Item BLOOD_IN_A_BOTTLE = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "blood_in_a_bottle"
    );

    public static final Item BINDING_CHAIN = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "binding_chain"
    );

    public static final Item CURSED_SKULL = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "cursed_skull"
    );

    public static final Item ENT_ROOT = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "ent_root"
    );

    public static final Item SACRED_LEAF = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "sacred_leaf"
    );

    public static final Item REDSTONE_CRYSTAL = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "redstone_crystal"
    );

    public static final Item WITHERED_VINE = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "withered_vine"
    );

    public static final Item PILE_OF_ASH = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "pile_of_ash"
    );

    public static final Item PURE_GOLD_INGOT = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "pure_gold_ingot"
    );

    public static final Item HOLY_WATER_FLASK = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "holy_water_flask"
    );

    public static final Item NETHERITE_BOWL = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "netherite_bowl"
    );

    public static final Item RARE_CARROT = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "rare_carrot"
    );

    public static final Item RARE_MEAT = ItemInit.register(
            new SimpleItem(new Item.Properties()),
            "rare_meat"
    );

    // XP ITEMS
    public static final Item XP_ITEM_1 = ItemInit.register(
            new SkillXpItem(new Item.Properties().stacksTo(64),100),
            "xp_item_1"
    );
    @SuppressWarnings("unused")
    public static final Item XP_ITEM_2 = ItemInit.register(
            new SkillXpItem(new Item.Properties().stacksTo(64),200),
            "xp_item_2"
    );
    public static final Item XP_ITEM_3 = ItemInit.register(
            new SkillXpItem(new Item.Properties().stacksTo(64),300),
            "xp_item_3"
    );
    public static final Item XP_ITEM_4 = ItemInit.register(
            new SkillXpItem(new Item.Properties().stacksTo(64),400),
            "xp_item_4"
    );
    public static final Item XP_ITEM_5 = ItemInit.register(
            new SkillXpItem(new Item.Properties().stacksTo(64),500),
            "xp_item_5"
    );

    public class ItemInit {
        public static Item register(Item item, String id) {
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
