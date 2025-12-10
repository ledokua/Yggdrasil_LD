package net.ledok.screen;

import net.ledok.block.entity.MobSpawnerBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MobAttributesScreenHandler extends AbstractContainerMenu {
    public final MobSpawnerBlockEntity blockEntity;
    public final List<MobSpawnerBlockEntity.AttributeData> attributes;

    public MobAttributesScreenHandler(int syncId, Inventory inventory, MobAttributesData data) {
        super(ModScreenHandlers.MOB_ATTRIBUTES_SCREEN_HANDLER, syncId);
        this.blockEntity = (MobSpawnerBlockEntity) inventory.player.level().getBlockEntity(data.pos());
        this.attributes = new ArrayList<>(blockEntity.attributes);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
