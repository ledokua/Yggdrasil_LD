package net.ledok.registry;

import net.ledok.YggdrasilLdMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTabRegistry {

    public static final CreativeModeTab YGGDRASIL_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.yggdrasil_ld.main"))
            .icon(() -> new ItemStack(ItemRegistry.ICON))
            .displayItems((displayParameters, output) -> {
                // Regular Items
                output.accept(new ItemStack(ItemRegistry.RESET_SCROLL));
                output.accept(new ItemStack(ItemRegistry.ESCAPE_SCROLL));
                output.accept(new ItemStack(ItemRegistry.DRIPSTONE_SCROLL));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_1));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_2));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_3));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_4));
                output.accept(new ItemStack(ItemRegistry.XP_ITEM_5));

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

                output.accept(new ItemStack(ItemRegistry.STONE_REINFORCED_HAMMER_HEAD));
                output.accept(new ItemStack(ItemRegistry.STONE_REINFORCED_HAMMER));
                output.accept(new ItemStack(ItemRegistry.STONE_IMPACT_HAMMER_HEAD));
                output.accept(new ItemStack(ItemRegistry.STONE_HEAD_IRON));
                output.accept(new ItemStack(ItemRegistry.REINFORCED_HAMMER_HANDLE));
                output.accept(new ItemStack(ItemRegistry.NETHERITE_REINFORCED_HAMMER_HEAD));
                output.accept(new ItemStack(ItemRegistry.NETHERITE_IMPACT_HAMMER_HEAD));
                output.accept(new ItemStack(ItemRegistry.IRON_REINFORCED_HAMMER_HEAD));
                output.accept(new ItemStack(ItemRegistry.IRON_IMPACT_HAMMER_HEAD));
                output.accept(new ItemStack(ItemRegistry.IMPACT_HAMMER_HANDLE));
                output.accept(new ItemStack(ItemRegistry.HAMMER_HANDLE));
                output.accept(new ItemStack(ItemRegistry.HAMMER_HEAD_NETHERITE));
                output.accept(new ItemStack(ItemRegistry.HAMMER_HEAD_IRON));
                output.accept(new ItemStack(ItemRegistry.HAMMER_HEAD_DIAMOND));
                output.accept(new ItemStack(ItemRegistry.GOLD_REINFORCED_HAMMER_HEAD));
                output.accept(new ItemStack(ItemRegistry.GOLD_IMPACT_HAMMER_HEAD));
                output.accept(new ItemStack(ItemRegistry.GOLD_HEAD_NETHERITE));
                output.accept(new ItemStack(ItemRegistry.DIAMOND_REINFORCED_HAMMER_HEAD));
                output.accept(new ItemStack(ItemRegistry.DIAMOND_IMPACT_HAMMER_HEAD));

                output.accept(new ItemStack(ItemRegistry.LIQUID_MANA_BUCKET));
                // Loot Boxes
                output.acceptAll(LootBoxRegistry.getAllLootBoxes());
            }).build();

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "main"), YGGDRASIL_TAB);
    }
}
