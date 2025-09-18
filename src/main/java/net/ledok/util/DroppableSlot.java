package net.ledok.util;

import net.minecraft.item.ItemStack;

/**
 * A helper record to store an ItemStack that is eligible for dropping,
 * along with the action required to clear it from its original inventory slot.
 */
public record DroppableSlot(ItemStack stack, Runnable clearSlotAction) {}
