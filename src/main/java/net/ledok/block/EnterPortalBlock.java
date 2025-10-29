package net.ledok.block;

import com.mojang.serialization.MapCodec;
import net.ledok.block.entity.EnterPortalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer; // Import ServerPlayer
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map
import java.util.UUID; // Import UUID

public class EnterPortalBlock extends BaseEntityBlock {

    public static final MapCodec<EnterPortalBlock> CODEC = simpleCodec(EnterPortalBlock::new);

    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private static final int COOLDOWN_TICKS = 100;


    public EnterPortalBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnterPortalBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide() && entity instanceof ServerPlayer player) {
            long currentTime = world.getGameTime();
            long cooldownEndTime = playerCooldowns.getOrDefault(player.getUUID(), 0L);

            if (player.canUsePortal(true) && currentTime >= cooldownEndTime) {
                if (world.getBlockEntity(pos) instanceof EnterPortalBlockEntity portalEntity) {
                    BlockPos destination = portalEntity.getDestination();
                    if(destination != null && world instanceof ServerLevel) {
                        // Teleport the player
                        player.teleportTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);

                        // --- Apply Cooldown ---
                        playerCooldowns.put(player.getUUID(), currentTime + COOLDOWN_TICKS);
                    }
                }
            }
        }
        if (!world.isClientSide() && world.getGameTime() % 1200 == 0) { // Every minute
            long currentTime = world.getGameTime();
            playerCooldowns.entrySet().removeIf(entry -> currentTime >= entry.getValue() + (COOLDOWN_TICKS * 10)); // Remove entries older than 10 cooldown periods
        }

    }
}
