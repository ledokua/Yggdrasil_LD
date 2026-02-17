package net.ledok.fluid;

import net.ledok.registry.BlockRegistry;
import net.ledok.registry.FluidRegistry;
import net.ledok.registry.ItemRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.Level;

public abstract class LiquidManaFluid extends BaseFluid {

    @Override
    public Fluid getSource() {
        return FluidRegistry.STILL_LIQUID_MANA;
    }

    @Override
    public Fluid getFlowing() {
        return FluidRegistry.FLOWING_LIQUID_MANA;
    }

    @Override
    public Item getBucket() {
        return ItemRegistry.LIQUID_MANA_BUCKET;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return BlockRegistry.LIQUID_MANA_BLOCK.defaultBlockState().setValue(BlockStateProperties.LEVEL, getLegacyLevel(state));
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return true;
    }

    public static class Flowing extends LiquidManaFluid {

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Still extends LiquidManaFluid {

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
