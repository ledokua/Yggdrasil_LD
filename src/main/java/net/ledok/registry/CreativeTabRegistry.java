package net.ledok.registry;

import net.ledok.YggdrasilLdMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTabRegistry {

    public static final CreativeModeTab YGGDRASIL_ITEMS_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.yggdrasil_ld.items"))
            .icon(() -> new ItemStack(ItemRegistry.ICON))
            .displayItems((displayParameters, output) -> {
                output.accept(new ItemStack(ItemRegistry.ANCIENT_ICE_SHARD));
                output.accept(new ItemStack(ItemRegistry.NETHERITE_BOTTLE));
                output.accept(new ItemStack(ItemRegistry.BAT_WING));
                output.accept(new ItemStack(ItemRegistry.BLOOD_IN_A_BOTTLE));
                output.accept(new ItemStack(ItemRegistry.BINDING_CHAIN));
                output.accept(new ItemStack(ItemRegistry.CURSED_SKULL));
                output.accept(new ItemStack(ItemRegistry.ENT_ROOT));
                output.accept(new ItemStack(ItemRegistry.SACRED_LEAF));
                output.accept(new ItemStack(ItemRegistry.REDSTONE_CRYSTAL));
                output.accept(new ItemStack(ItemRegistry.WITHERED_VINE));
                output.accept(new ItemStack(ItemRegistry.PILE_OF_ASH));
                output.accept(new ItemStack(ItemRegistry.PURE_GOLD_INGOT));
                output.accept(new ItemStack(ItemRegistry.HOLY_WATER_FLASK));
                output.accept(new ItemStack(ItemRegistry.NETHERITE_BOWL));
                output.accept(new ItemStack(ItemRegistry.RARE_CARROT));
                output.accept(new ItemStack(ItemRegistry.RARE_MEAT));
                output.accept(new ItemStack(ItemRegistry.RESET_SCROLL));
                output.accept(new ItemStack(ItemRegistry.ESCAPE_SCROLL));
                output.accept(new ItemStack(ItemRegistry.DRIPSTONE_SCROLL));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_1));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_2));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_3));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_4));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_5));
            }).build();

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "items"), YGGDRASIL_ITEMS_TAB);
    }
}
