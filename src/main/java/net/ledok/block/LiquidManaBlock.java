package net.ledok.block;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LiquidManaBlock extends LiquidBlock {
    // This map will store the last time an entity was healed.
    // We use a map here to avoid the API issues we were having.
    private final Map<UUID, Long> lastHealTickMap = new HashMap<>();

    public LiquidManaBlock(FlowingFluid fluid, Properties settings) {
        super(fluid, settings);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (world.isClientSide) {
            return; // Only apply effects on the server
        }

        if (entity instanceof LivingEntity livingEntity) {
            // Check if it's time to heal (once per second)
            if (world.getGameTime() % 20 == 0) {
                long lastHealTime = lastHealTickMap.getOrDefault(livingEntity.getUUID(), 0L);

                // Check if we have already healed the entity in this exact tick
                if (lastHealTime != world.getGameTime()) {
                    // If not, apply the heal and then update the map with the current time
                    lastHealTickMap.put(livingEntity.getUUID(), world.getGameTime());

                    float maxHealth = livingEntity.getMaxHealth();
                    float healAmount = maxHealth * 0.05f; // 5% of max health

                    livingEntity.heal(healAmount);
                }
            }
        }

        super.entityInside(state, world, pos, entity);
    }
}
