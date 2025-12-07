package net.ledok.registry;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ArmorMaterialRegistry {

    public static final Holder<ArmorMaterial> PRIEST_T6 = register("priest_t6", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
        enumMap.put(ArmorItem.Type.BOOTS, 3);
        enumMap.put(ArmorItem.Type.LEGGINGS, 6);
        enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
        enumMap.put(ArmorItem.Type.HELMET, 3);
        enumMap.put(ArmorItem.Type.BODY, 11);
    }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 2.0F, 0.12F, () -> Ingredient.of(Items.NETHERITE_INGOT));

    public static final Holder<ArmorMaterial> FROST_T6 = register("frost_t6", Util.make(new EnumMap(ArmorItem.Type.class), enumMap -> {
        enumMap.put(ArmorItem.Type.BOOTS, 3);
        enumMap.put(ArmorItem.Type.LEGGINGS, 6);
        enumMap.put(ArmorItem.Type.CHESTPLATE, 8);
        enumMap.put(ArmorItem.Type.HELMET, 3);
        enumMap.put(ArmorItem.Type.BODY, 11);
    }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 2.0F, 0.12F, () -> Ingredient.of(Items.NETHERITE_INGOT));

    private static Holder<ArmorMaterial> register(
            String string, EnumMap<ArmorItem.Type, Integer> enumMap, int i, Holder<SoundEvent> holder, float f, float g, Supplier<Ingredient> supplier
    ) {
        List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace(string)));
        return register(string, enumMap, i, holder, f, g, supplier, list);
    }

    private static Holder<ArmorMaterial> register(
            String string,
            EnumMap<ArmorItem.Type, Integer> enumMap,
            int i,
            Holder<SoundEvent> holder,
            float f,
            float g,
            Supplier<Ingredient> supplier,
            List<ArmorMaterial.Layer> list
    ) {
        EnumMap<ArmorItem.Type, Integer> enumMap2 = new EnumMap(ArmorItem.Type.class);

        return Registry.registerForHolder(
                BuiltInRegistries.ARMOR_MATERIAL, ResourceLocation.withDefaultNamespace(string), new ArmorMaterial(enumMap2, i, holder, supplier, list, f, g)
        );
    }

    public static void initialize() {
    }
}
