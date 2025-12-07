package net.ledok.block;

import com.mojang.serialization.MapCodec;
import net.ledok.block.entity.EnterPortalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction; // Import Direction
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer; // Import ServerPlayer
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext; // Import BlockPlaceContext
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*; // Import Block, Rotation, Mirror
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition; // Import StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties; // Import BlockStateProperties
import net.minecraft.world.level.block.state.properties.DirectionProperty; // Import DirectionProperty
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map
import java.util.UUID; // Import UUID

public class EnterPortalBlock extends BaseEntityBlock {

    public static final MapCodec<EnterPortalBlock> CODEC = simpleCodec(EnterPortalBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private static final int COOLDOWN_TICKS = 100;


    public EnterPortalBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.EAST));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnterPortalBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
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