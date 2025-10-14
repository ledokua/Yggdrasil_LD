package net.ledok.util;

import net.minecraft.item.ItemStack;

public record DroppableSlot(ItemStack stack, Runnable clearSlotAction) {}
