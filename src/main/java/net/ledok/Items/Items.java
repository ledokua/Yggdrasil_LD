package net.ledok.Items;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.ledok.Init.ItemInit;

import java.util.Collections;
import java.util.Optional;

public class Items {

    public static final Item ICON = ItemInit.register(
            new Item(new Item.Settings ()),
            "icon"
    );
    public static final Item RESET_SCROLL = ItemInit.register(
            new SkillResetItem(new Item.Settings().maxCount(16)),
            "reset_scroll"
    );

    public static void initialize() {
    }
}