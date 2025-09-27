package net.ledok.compat;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.ledok.util.DroppableSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class handles all direct interactions with the Trinkets mod.
 * It should ONLY be called after checking if the mod is loaded to prevent crashes.
 */
public class TrinketsCompat {

    /**
     * Gets a list of all equipped trinkets as DroppableSlots.
     * @param player The player to check.
     * @return A list of droppable slots for each equipped trinket.
     */
    public static List<DroppableSlot> getTrinketSlots(PlayerEntity player) {
        List<DroppableSlot> trinketSlots = new ArrayList<>();
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);

        if (component.isPresent()) {
            // Get all equipped trinkets (the predicate `stack -> true` matches all items)
            component.get().getEquipped(stack -> true).forEach(pair -> {
                ItemStack trinketStack = pair.getRight();
                if (!trinketStack.isEmpty()) {
                    // The clear action uses the SlotReference from the pair to set the specific slot to empty.
                    Runnable clearSlotAction = () -> pair.getLeft().inventory().setStack(pair.getLeft().index(), ItemStack.EMPTY);
                    trinketSlots.add(new DroppableSlot(trinketStack.copy(), clearSlotAction));
                }
            });
        }
        return trinketSlots;
    }
}