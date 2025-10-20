package net.ledok.registry;

import net.ledok.Init.ItemInit;
import net.ledok.Items.FrostT6ArmorItem;
import net.ledok.Items.PriestT6ArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ArmorRegistry {

    // PRIEST_ARMOR_T6
    public static final Item PRIEST_ARMOR_HELMET = ItemInit.register(
            new PriestT6ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)),
            "t6_priest_helmet"
    );
    public static final Item PRIEST_ARMOR_CHESTPLATE = ItemInit.register(
            new PriestT6ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)),
            "t6_priest_chestplate"
    );
    public static final Item PRIEST_ARMOR_LEGGINGS = ItemInit.register(
            new PriestT6ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)),
            "t6_priest_leggings"
    );
    public static final Item PRIEST_ARMOR_BOOTS = ItemInit.register(
            new PriestT6ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)),
            "t6_priest_boots"
    );

    // FROST_ARMOR_T6
    public static final Item FROST_ARMOR_HELMET = ItemInit.register(
            new FrostT6ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)),
            "t6_frost_helmet"
    );
    public static final Item FROST_ARMOR_CHESTPLATE = ItemInit.register(
            new FrostT6ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)),
            "t6_frost_chestplate"
    );
    public static final Item FROST_ARMOR_LEGGINGS = ItemInit.register(
            new FrostT6ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)),
            "t6_frost_leggings"
    );
    public static final Item FROST_ARMOR_BOOTS = ItemInit.register(
            new FrostT6ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)),
            "t6_frost_boots"
    );

    public static void initialize() {
    }
}
