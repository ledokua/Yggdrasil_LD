package net.ledok.Init;

import net.ledok.YggdrasilLdMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

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