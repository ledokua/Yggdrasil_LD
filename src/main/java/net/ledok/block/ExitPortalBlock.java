package net.ledok.block;

import com.mojang.serialization.MapCodec;
import net.ledok.block.entity.ExitPortalBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class ExitPortalBlock extends BlockWithEntity {

    public static final MapCodec<ExitPortalBlock> CODEC = createCodec(ExitPortalBlock::new);

    public ExitPortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ExitPortalBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient() && entity instanceof PlayerEntity player) {
            if (world.getBlockEntity(pos) instanceof ExitPortalBlockEntity portalEntity) {
                BlockPos destination = portalEntity.getDestination();
                if(destination != null && world instanceof ServerWorld serverWorld) {
                    // --- FIX: Use the correct, modern teleport method signature ---
                    player.teleport(serverWorld, destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, Collections.emptySet(), player.getYaw(), player.getPitch());
                }
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof ExitPortalBlockEntity be) {
                ExitPortalBlockEntity.tick(world1, pos, state1, be);
            }
        };
    }
}

