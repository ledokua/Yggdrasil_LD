package net.ledok.compat;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.ledok.util.DroppableSlot;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
    public static List<DroppableSlot> getTrinketSlots(Player player) {
        List<DroppableSlot> trinketSlots = new ArrayList<>();
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);

        if (component.isPresent()) {
            // Correct the type to what the compiler is finding: Tuple
            List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getEquipped(stack -> true);
            allEquipped.forEach(tuple -> { // Use a different variable name to avoid confusion
                ItemStack trinketStack = tuple.getB(); // Tuple uses getA() and getB()
                if (!trinketStack.isEmpty()) {
                    SlotReference slotReference = tuple.getA();
                    Runnable clearSlotAction = () -> slotReference.inventory().setItem(slotReference.index(), ItemStack.EMPTY);
                    trinketSlots.add(new DroppableSlot(trinketStack.copy(), clearSlotAction));
                }
            });
        }
        return trinketSlots;
    }
}

