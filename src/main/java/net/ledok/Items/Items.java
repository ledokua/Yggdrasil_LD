package net.ledok.Items;

import net.minecraft.item.Item;
import net.ledok.Init.ItemInit;

public class Items {

    public static final Item ICON = ItemInit.register(
            new Item(new Item.Settings ()),
            "icon"
    );
    public static final Item RESET_SCROLL = ItemInit.register(
            new SkillResetItem(new Item.Settings().maxCount(16)),
            "reset_scroll"
    );
    public static final Item ESCAPE_SCROLL = ItemInit.register(
            new DungeonLeaveItem(new Item.Settings().maxCount(1)),
            "escape_scroll"
    );
    public static final Item DRIPSTONE_SCROLL = ItemInit.register(
            new RandomDripstoneItem(new Item.Settings().maxCount(1)),
            "dripstone_scroll"
    );
    public static final Item HEALING_POTION_1 = ItemInit.register(
            // healPercentage: 10%, useTimeTicks: 3s, cooldownTicks: 5s
            new PercentageHealItem(new Item.Settings().maxCount(8), 0.1f, 60, 100),
            "healing_potion_1"
    );
    public static final Item HEALING_POTION_2 = ItemInit.register(
            // healPercentage: 25%, useTimeTicks: 3s, cooldownTicks: 35s
            new PercentageHealItem(new Item.Settings().maxCount(16), 0.25f, 60, 700),
            "healing_potion_2"
    );
    public static final Item HEALING_POTION_3 = ItemInit.register(
            // healPercentage: 35%, useTimeTicks: 1.25s, cooldownTicks: 50s
            new PercentageHealItem(new Item.Settings().maxCount(16), 0.35f, 80, 1000),
            "healing_potion_3"
    );
    public static final Item HEALING_POTION_4 = ItemInit.register(
            // healPercentage: 50%, useTimeTicks: 7s, cooldownTicks: 60s
            new PercentageHealItem(new Item.Settings().maxCount(16), 0.5f, 140, 1200),
            "healing_potion_4"
    );
    public static final Item HEALING_POTION_5 = ItemInit.register(
            // healPercentage: 75%, useTimeTicks: 10s, cooldownTicks: 100s
            new PercentageHealItem(new Item.Settings().maxCount(16), 0.75f, 140, 2000),
            "healing_potion_5"
    );
    public static final Item HEALING_POTION_6 = ItemInit.register(
            // healPercentage: 100%, useTimeTicks: 0.5s, cooldownTicks: 10m
            new PercentageHealItem(new Item.Settings().maxCount(1), 1f, 10, 12000),
            "healing_potion_6"
    );

    public static void initialize() {
    }
}