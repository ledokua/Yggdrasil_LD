package net.ledok.mixin;

import net.ledok.Yggdrasil_ld;
import net.ledok.reputation.ReputationManager;
import net.ledok.util.PvPContextManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow public abstract PlayerInventory getInventory();

    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    private void yggdrasil_partialKeepInventory(CallbackInfo ci) {
        PlayerEntity victim = (PlayerEntity) (Object) this;
        if (!victim.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            return;
        }

        UUID attackerUuid = PvPContextManager.getAttacker();
        try {
            boolean isPredatoryKill = false;
            if (attackerUuid != null && Yggdrasil_ld.CONFIG.predatory_kill_enabled) {
                ServerPlayerEntity attacker = victim.getServer().getPlayerManager().getPlayer(attackerUuid);
                if (attacker != null) {
                    int attackerRep = ReputationManager.getReputation(attacker);
                    int victimRep = ReputationManager.getReputation(victim);
                    if (attackerRep < 0 && victimRep >= Yggdrasil_ld.CONFIG.predatory_kill_victim_positive_rep_threshold) {
                        isPredatoryKill = true;
                    }
                }
            }

            if (isPredatoryKill) {
                // --- Predatory kill logic ---
                ServerPlayerEntity attacker = victim.getServer().getPlayerManager().getPlayer(attackerUuid);
                int attackerRep = ReputationManager.getReputation(attacker);
                List<Integer> slotsToDrop = new ArrayList<>();

                // 1. How much should drop calculation
                int equipItemsToDrop = Math.abs(attackerRep) / Yggdrasil_ld.CONFIG.predatory_kill_equipment_drop_rep_step;
                equipItemsToDrop = Math.min(equipItemsToDrop, Yggdrasil_ld.CONFIG.predatory_kill_equipment_drop_max);
                addRandomSlotsToList(slotsToDrop, getEquipmentSlots(), equipItemsToDrop);

                // 2. How much should drop from main inv
                int invItemsToDrop = Math.abs(attackerRep) / Yggdrasil_ld.CONFIG.predatory_kill_inventory_drop_rep_step;
                addRandomSlotsToList(slotsToDrop, getMainInventorySlots(slotsToDrop), invItemsToDrop);

                // END
                dropItemsFromSlots(victim, slotsToDrop);

            } else {
                // --- Normal logic for all deaths ---
                List<Integer> slotsToDrop = new ArrayList<>();
                int reputation = ReputationManager.getReputation(victim);

                // 1. Penalty for lov reputation
                if (reputation <= Yggdrasil_ld.CONFIG.reputation_penalty_threshold) {
                    addRandomSlotsToList(slotsToDrop, getEquipmentSlots(), Yggdrasil_ld.CONFIG.reputation_penalty_item_count);
                }

                // 2. Additional % calculation
                double baseDropPercentage = Yggdrasil_ld.CONFIG.keep_inventory_drop_percentage;
                double finalDropPercentage = baseDropPercentage;
                if (Yggdrasil_ld.CONFIG.reputation_affects_drops) {
                    // Every 20p of rep = 1%
                    finalDropPercentage -= (double)reputation / 20.0;
                }
                finalDropPercentage = Math.max(0, Math.min(100, finalDropPercentage));

                if (finalDropPercentage > 0) {
                    List<Integer> mainInvSlots = getMainInventorySlots(slotsToDrop);
                    int itemsToDropCount = (int) Math.floor(mainInvSlots.size() * (finalDropPercentage / 100.0));
                    addRandomSlotsToList(slotsToDrop, mainInvSlots, itemsToDropCount);
                }

                // END
                dropItemsFromSlots(victim, slotsToDrop);
            }

            // NO VANILLA!!!!
            ci.cancel();

        } finally {
            PvPContextManager.clear();
        }
    }

    // --- Helping methods ---

    private List<Integer> getEquipmentSlots() {
        List<Integer> slots = new ArrayList<>();
        slots.addAll(getSlots(0, 8)); // Hotbar
        slots.addAll(getSlots(36, 39)); // Armor
        return slots;
    }

    private List<Integer> getMainInventorySlots(List<Integer> exclude) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 9; i <= 35; i++) { // Inventory
            if (!getInventory().getStack(i).isEmpty() && !exclude.contains(i)) {
                slots.add(i);
            }
        }
        return slots;
    }

    private void addRandomSlotsToList(List<Integer> targetList, List<Integer> sourceSlots, int count) {
        Collections.shuffle(sourceSlots);
        int added = 0;
        for (int slot : sourceSlots) {
            if (added >= count) break;
            if (!targetList.contains(slot)) {
                targetList.add(slot);
                added++;
            }
        }
    }

    private void dropItemsFromSlots(PlayerEntity player, List<Integer> slots) {
        for (int slotIndex : slots) {
            ItemStack stack = getInventory().getStack(slotIndex);
            if (!stack.isEmpty()) {
                player.dropStack(stack);
                getInventory().setStack(slotIndex, ItemStack.EMPTY);
            }
        }
    }

    private List<Integer> getSlots(int start, int end) {
        List<Integer> slots = new ArrayList<>();
        PlayerInventory inventory = this.getInventory();
        if (start >= 36 && end <= 39) {
            for (int i = 0; i < inventory.armor.size(); i++) {
                if (!inventory.armor.get(i).isEmpty()) slots.add(36 + i);
            }
        } else {
            for (int i = start; i <= end; i++) {
                if (!getInventory().getStack(i).isEmpty()) slots.add(i);
            }
        }
        return slots;
    }
}

