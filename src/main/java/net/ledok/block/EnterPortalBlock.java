package net.ledok.block;

import com.mojang.serialization.MapCodec;
import net.ledok.block.entity.EnterPortalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EnterPortalBlock extends BaseEntityBlock {

    public static final MapCodec<EnterPortalBlock> CODEC = simpleCodec(EnterPortalBlock::new);

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
        if (!world.isClientSide() && entity instanceof Player player) {
            if (player.canUsePortal(true)) {
                if (world.getBlockEntity(pos) instanceof EnterPortalBlockEntity portalEntity) {
                    BlockPos destination = portalEntity.getDestination();
                    if(destination != null && world instanceof ServerLevel) {
                        player.teleportTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);
                    }
                }
            }
        }
    }
}
