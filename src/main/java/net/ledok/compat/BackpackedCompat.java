package net.ledok.compat;

import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.common.backpack.UnlockableSlots;
import net.ledok.util.DroppableSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles all direct interactions with the Backpacked mod.
 * It should ONLY be called after checking if the mod is loaded to prevent crashes.
 */
public class BackpackedCompat {

    /**
     * Gets ALL equipped and unlocked backpacks as a list of DroppableSlots.
     * Each slot contains the backpack ItemStack and the logic to remove it from the player.
     * @param player The player to check.
     * @return A list containing all found backpack slots.
     */
    public static List<DroppableSlot> getAllEquippedBackpacks(PlayerEntity player) {
        List<DroppableSlot> slots = new ArrayList<>();

        // Get all possible backpack stacks and which slots are unlocked
        DefaultedList<ItemStack> backpacks = BackpackHelper.getBackpacks(player);
        UnlockableSlots unlockedSlots = BackpackHelper.getBackpackUnlockableSlots(player);

        // Find all unlocked slots that contain a backpack
        for (int i = 0; i < backpacks.size(); i++) {
            if (unlockedSlots.isUnlocked(i)) {
                ItemStack backpackStack = backpacks.get(i);
                if (!backpackStack.isEmpty()) {
                    // We found one, create the clear action for this specific slot
                    final int slotIndex = i;
                    Runnable clearSlotAction = () -> {
                        // We get the list again to ensure it's the most current state
                        DefaultedList<ItemStack> currentBackpacks = BackpackHelper.getBackpacks(player);
                        if (slotIndex < currentBackpacks.size()) {
                            currentBackpacks.set(slotIndex, ItemStack.EMPTY);
                        }
                    };
                    slots.add(new DroppableSlot(backpackStack.copy(), clearSlotAction));
                }
            }
        }
        return slots;
    }
}

