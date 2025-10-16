package net.ledok.util;

import net.minecraft.world.item.ItemStack;

/**
 * A simple record to bundle an ItemStack with the action required to clear it
 * from its original inventory slot. This is used for the partial keep-inventory system.
 * As a record, it automatically provides accessor methods like `stack()` and `clearSlotAction()`.
 */
public record DroppableSlot(ItemStack stack, Runnable clearSlotAction) {
}
