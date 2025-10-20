package net.ledok.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.ledok.YggdrasilLdMod;
import net.ledok.compat.BackpackedCompat;
import net.ledok.compat.TrinketsCompat; // Import the new compatibility class
import net.ledok.reputation.ReputationManager;
import net.ledok.util.DroppableSlot;
import net.ledok.util.PvPContextManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {

    @Shadow public abstract Inventory getInventory();

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void yggdrasil_partialKeepInventory(CallbackInfo ci) {

        if (!YggdrasilLdMod.CONFIG.partial_inventory_save_enabled) {
            return;
        }

        Player victim = (Player) (Object) this;
        // level() is the new getWorld(), getGameRules() is now part of the level
        if (!victim.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        UUID attackerUuid = PvPContextManager.getAttacker();
        try {
            boolean isPredatoryKill = false;
            if (attackerUuid != null && YggdrasilLdMod.CONFIG.predatory_kill_enabled) {
                // FIX: The 'server' field is no longer directly on the Player entity. Use getServer() instead.
                ServerPlayer attacker = victim.getServer().getPlayerList().getPlayer(attackerUuid);
                if (attacker != null) {
                    int attackerRep = ReputationManager.getReputation(attacker);
                    int victimRep = ReputationManager.getReputation(victim);
                    if (attackerRep < 0 && victimRep >= YggdrasilLdMod.CONFIG.predatory_kill_victim_positive_rep_threshold) {
                        isPredatoryKill = true;
                    }
                }
            }

            if (isPredatoryKill) {
                handlePredatoryKillDrops(victim, attackerUuid);
            } else {
                handleStandardDeathDrops(victim);
            }

            ci.cancel();

        } finally {
            PvPContextManager.clear();
        }
    }

    private void handlePredatoryKillDrops(Player victim, UUID attackerUuid) {
        // FIX: The 'server' field is no longer directly on the Player entity. Use getServer() instead.
        ServerPlayer attacker = victim.getServer().getPlayerList().getPlayer(attackerUuid);
        if (attacker == null) return;

        int attackerRep = ReputationManager.getReputation(attacker);
        List<DroppableSlot> finalSlotsToDrop = new ArrayList<>();

        // --- Standard Equipment & Inventory Drops ---
        List<DroppableSlot> equipmentPool = getEquipmentSlots(victim);
        equipmentPool.addAll(getTrinketSlots(victim)); // Add Trinkets to the equipment pool
        int equipItemsToDrop = Math.abs(attackerRep) / YggdrasilLdMod.CONFIG.predatory_kill_equipment_drop_rep_step;
        equipItemsToDrop = Math.min(equipItemsToDrop, YggdrasilLdMod.CONFIG.predatory_kill_equipment_drop_max);
        addRandomSlotsToList(finalSlotsToDrop, equipmentPool, equipItemsToDrop);

        List<DroppableSlot> mainInvPool = getMainInventorySlots(victim);
        int invItemsToDrop = Math.abs(attackerRep) / YggdrasilLdMod.CONFIG.predatory_kill_inventory_drop_rep_step;
        addRandomSlotsToList(finalSlotsToDrop, mainInvPool, invItemsToDrop);

        // --- Backpack Drops for Predatory Kills ---
        int additionalBackpacks = Math.abs(attackerRep) / 5000;
        additionalBackpacks = Math.min(additionalBackpacks, 2);
        calculateAndAddBackpackDrops(finalSlotsToDrop, victim, additionalBackpacks);

        dropItemsFromSlots(victim, finalSlotsToDrop);
    }

    private void handleStandardDeathDrops(Player victim) {
        List<DroppableSlot> finalSlotsToDrop = new ArrayList<>();
        int reputation = ReputationManager.getReputation(victim);

        // --- Standard Percentage Drops ---
        double baseDropPercentage = YggdrasilLdMod.CONFIG.keep_inventory_drop_percentage;
        double finalDropPercentage = baseDropPercentage;
        if (YggdrasilLdMod.CONFIG.reputation_affects_drops) {
            finalDropPercentage -= (double)reputation / 20.0;
        }
        finalDropPercentage = Math.max(0, Math.min(100, finalDropPercentage));

        if (finalDropPercentage > 0) {
            List<DroppableSlot> mainInvPool = getMainInventorySlots(victim);
            int itemsToDropCount = (int) Math.floor(mainInvPool.size() * (finalDropPercentage / 100.0));
            addRandomSlotsToList(finalSlotsToDrop, mainInvPool, itemsToDropCount);
        }

        // --- Additional Equipment Penalty ---
        if (reputation <= YggdrasilLdMod.CONFIG.reputation_penalty_threshold) {
            List<DroppableSlot> equipmentPool = getEquipmentSlots(victim);
            equipmentPool.addAll(getTrinketSlots(victim)); // Add Trinkets to the equipment pool
            addRandomSlotsToList(finalSlotsToDrop, equipmentPool, YggdrasilLdMod.CONFIG.reputation_penalty_item_count);
        }

        // --- Dedicated Backpack Drop Logic ---
        calculateAndAddBackpackDrops(finalSlotsToDrop, victim, 0);

        dropItemsFromSlots(victim, finalSlotsToDrop);
    }

    private void calculateAndAddBackpackDrops(List<DroppableSlot> finalSlotsToDrop, Player victim, int additionalPredatoryDrops) {
        int victimRep = ReputationManager.getReputation(victim);
        int backpacksToDrop = 0;
        RandomSource random = victim.level().random;

        if (victimRep >= 1000) {
            backpacksToDrop = 0;
        } else if (victimRep >= 100) {
            double chance = 0.50 - ((victimRep - 100.0) / 900.0) * 0.50;
            if (random.nextDouble() < chance) {
                backpacksToDrop = 1;
            }
        } else if (victimRep >= 0) {
            backpacksToDrop = 1;
        } else {
            if (victimRep >= -499)      backpacksToDrop = 1;
            else if (victimRep >= -999) backpacksToDrop = 2;
            else if (victimRep >= -1499)backpacksToDrop = 3;
            else if (victimRep >= -1999)backpacksToDrop = 4;
            else                        backpacksToDrop = 5;
        }

        backpacksToDrop += additionalPredatoryDrops;

        if (backpacksToDrop > 0) {
            List<DroppableSlot> backpackPool = getAllEquippedBackpacks(victim);
            addRandomSlotsToList(finalSlotsToDrop, backpackPool, backpacksToDrop);
        }
    }

    private List<DroppableSlot> getEquipmentSlots(Player player) {
        List<DroppableSlot> slots = new ArrayList<>();
        Inventory inventory = player.getInventory();
        // Armor
        for (int i = 0; i < inventory.armor.size(); i++) {
            final int slotIndex = i;
            addSlotIfNotEmpty(slots, inventory.armor.get(slotIndex), () -> inventory.armor.set(slotIndex, ItemStack.EMPTY));
        }
        // Hotbar (slots 0-8 of the main inventory)
        for (int i = 0; i <= 8; i++) {
            final int slotIndex = i;
            addSlotIfNotEmpty(slots, inventory.items.get(slotIndex), () -> inventory.items.set(slotIndex, ItemStack.EMPTY));
        }
        // Offhand
        addSlotIfNotEmpty(slots, inventory.offhand.get(0), () -> inventory.offhand.set(0, ItemStack.EMPTY));
        return slots;
    }

    private List<DroppableSlot> getMainInventorySlots(Player player) {
        List<DroppableSlot> slots = new ArrayList<>();
        Inventory inventory = player.getInventory();
        // Main inventory (slots 9-35)
        for (int i = 9; i <= 35; i++) {
            final int slotIndex = i;
            addSlotIfNotEmpty(slots, inventory.items.get(slotIndex), () -> inventory.items.set(slotIndex, ItemStack.EMPTY));
        }
        return slots;
    }

    private List<DroppableSlot> getAllEquippedBackpacks(Player player) {
        if (FabricLoader.getInstance().isModLoaded("backpacked")) {
            return BackpackedCompat.getAllEquippedBackpacks(player);
        }
        return new ArrayList<>();
    }

    // --- HELPER FOR TRINKETS ---
    private List<DroppableSlot> getTrinketSlots(Player player) {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            return TrinketsCompat.getTrinketSlots(player);
        }
        return new ArrayList<>();
    }

    private void addSlotIfNotEmpty(List<DroppableSlot> list, ItemStack stack, Runnable clearAction) {
        if (!stack.isEmpty()) {
            list.add(new DroppableSlot(stack.copy(), clearAction));
        }
    }

    private void addRandomSlotsToList(List<DroppableSlot> targetList, List<DroppableSlot> sourceSlots, int count) {
        if (count <= 0 || sourceSlots.isEmpty()) {
            return;
        }
        Collections.shuffle(sourceSlots);
        int amountToAdd = Math.min(sourceSlots.size(), count);
        for(int i = 0; i < amountToAdd; i++) {
            targetList.add(sourceSlots.get(i));
        }
    }

    private void dropItemsFromSlots(Player player, List<DroppableSlot> slots) {
        for (DroppableSlot slot : slots) {
            // dropStack is now drop(stack, dropAround, trace)
            player.drop(slot.stack(), true, false);
            // FIX: The accessor for a record component is a method call.
            // This now correctly calls the action to clear the item from its original slot.
            slot.clearSlotAction().run();
        }
    }
}

