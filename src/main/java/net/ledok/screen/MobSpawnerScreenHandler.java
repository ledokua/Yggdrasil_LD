package net.ledok.screen;

import net.ledok.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class MobSpawnerScreenHandler extends ScreenHandler {
    public final MobSpawnerBlockEntity blockEntity;
    public final PlayerEntity player;

    public MobSpawnerScreenHandler(int syncId, PlayerInventory playerInventory, MobSpawnerData data) {
        this(syncId, playerInventory, (MobSpawnerBlockEntity) playerInventory.player.getWorld().getBlockEntity(data.blockPos()));
    }

    public MobSpawnerScreenHandler(int syncId, PlayerInventory playerInventory, MobSpawnerBlockEntity blockEntity) {
        super(ModScreenHandlers.MOB_SPAWNER_SCREEN_HANDLER, syncId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
