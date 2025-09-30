package net.ledok.block;

import com.mojang.serialization.MapCodec;
import net.ledok.block.entity.BossSpawnerBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BossSpawnerBlock extends BlockWithEntity {

    public static final MapCodec<BossSpawnerBlock> CODEC = createCodec(BossSpawnerBlock::new);

    public BossSpawnerBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BossSpawnerBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            // --- NEW: Permission Check ---
            if (!player.isCreative() && !player.hasPermissionLevel(2)) {
                player.sendMessage(Text.literal("You don't have permission to configure this block.").formatted(Formatting.RED), false);
                return ActionResult.FAIL;
            }

            NamedScreenHandlerFactory screenHandlerFactory = world.getBlockEntity(pos) instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory) world.getBlockEntity(pos) : null;
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof BossSpawnerBlockEntity be) {
                BossSpawnerBlockEntity.tick(world1, pos, state1, be);
            }
        };
    }
}
