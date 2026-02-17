package net.ledok.registry;

import net.ledok.YggdrasilLdMod;
import net.ledok.fluid.LiquidManaFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public class FluidRegistry {
    public static final FlowingFluid STILL_LIQUID_MANA = Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "liquid_mana"), new LiquidManaFluid.Still());
    public static final FlowingFluid FLOWING_LIQUID_MANA = Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "flowing_liquid_mana"), new LiquidManaFluid.Flowing());

    public static void initialize() {
    }
}
