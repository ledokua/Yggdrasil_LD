package net.ledok.screen;

import net.ledok.block.entity.BossSpawnerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class BossSpawnerScreenHandler extends ScreenHandler {
    public final BossSpawnerBlockEntity blockEntity;
    public final PlayerEntity player;

    // --- Constructor to receive the BossSpawnerData object ---
    public BossSpawnerScreenHandler(int syncId, PlayerInventory playerInventory, BossSpawnerData data) {
        this(syncId, playerInventory, (BossSpawnerBlockEntity) playerInventory.player.getWorld().getBlockEntity(data.blockPos()));
    }

    public BossSpawnerScreenHandler(int syncId, PlayerInventory playerInventory, BossSpawnerBlockEntity blockEntity) {
        super(ModScreenHandlers.BOSS_SPAWNER_SCREEN_HANDLER, syncId);
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

