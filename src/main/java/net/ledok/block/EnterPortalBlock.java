package net.ledok.block;

import com.mojang.serialization.MapCodec;
import net.ledok.block.entity.EnterPortalBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class EnterPortalBlock extends BlockWithEntity {

    public static final MapCodec<EnterPortalBlock> CODEC = createCodec(EnterPortalBlock::new);

    public EnterPortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnterPortalBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient() && entity instanceof PlayerEntity player) {
            if (world.getBlockEntity(pos) instanceof EnterPortalBlockEntity portalEntity) {
                BlockPos destination = portalEntity.getDestination();
                if(destination != null && world instanceof ServerWorld serverWorld) {
                    player.teleport(serverWorld, destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, Collections.emptySet(), player.getYaw(), player.getPitch());
                }
            }
        }
    }
}
