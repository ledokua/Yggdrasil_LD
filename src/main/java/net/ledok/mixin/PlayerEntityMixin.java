package net.ledok.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.ledok.Yggdrasil_ld;
import net.ledok.compat.BackpackedCompat;
import net.ledok.compat.TrinketsCompat; // Import the new compatibility class
import net.ledok.reputation.ReputationManager;
import net.ledok.util.DroppableSlot;
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

        if (!Yggdrasil_ld.CONFIG.partial_inventory_save_enabled) {
            return;
        }

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
                handlePredatoryKillDrops(victim, attackerUuid);
            } else {
                handleStandardDeathDrops(victim);
            }

            ci.cancel();

        } finally {
            PvPContextManager.clear();
        }
    }

    private void handlePredatoryKillDrops(PlayerEntity victim, UUID attackerUuid) {
        ServerPlayerEntity attacker = victim.getServer().getPlayerManager().getPlayer(attackerUuid);
        if (attacker == null) return;

        int attackerRep = ReputationManager.getReputation(attacker);
        List<DroppableSlot> finalSlotsToDrop = new ArrayList<>();

        // --- Standard Equipment & Inventory Drops ---
        List<DroppableSlot> equipmentPool = getEquipmentSlots(victim);
        equipmentPool.addAll(getTrinketSlots(victim)); // Add Trinkets to the equipment pool
        int equipItemsToDrop = Math.abs(attackerRep) / Yggdrasil_ld.CONFIG.predatory_kill_equipment_drop_rep_step;
        equipItemsToDrop = Math.min(equipItemsToDrop, Yggdrasil_ld.CONFIG.predatory_kill_equipment_drop_max);
        addRandomSlotsToList(finalSlotsToDrop, equipmentPool, equipItemsToDrop);

        List<DroppableSlot> mainInvPool = getMainInventorySlots(victim);
        int invItemsToDrop = Math.abs(attackerRep) / Yggdrasil_ld.CONFIG.predatory_kill_inventory_drop_rep_step;
        addRandomSlotsToList(finalSlotsToDrop, mainInvPool, invItemsToDrop);

        // --- Backpack Drops for Predatory Kills ---
        int additionalBackpacks = Math.abs(attackerRep) / 5000;
        additionalBackpacks = Math.min(additionalBackpacks, 2);
        calculateAndAddBackpackDrops(finalSlotsToDrop, victim, additionalBackpacks);

        dropItemsFromSlots(victim, finalSlotsToDrop);
    }

    private void handleStandardDeathDrops(PlayerEntity victim) {
        List<DroppableSlot> finalSlotsToDrop = new ArrayList<>();
        int reputation = ReputationManager.getReputation(victim);

        // --- Standard Percentage Drops ---
        double baseDropPercentage = Yggdrasil_ld.CONFIG.keep_inventory_drop_percentage;
        double finalDropPercentage = baseDropPercentage;
        if (Yggdrasil_ld.CONFIG.reputation_affects_drops) {
            finalDropPercentage -= (double)reputation / 20.0;
        }
        finalDropPercentage = Math.max(0, Math.min(100, finalDropPercentage));

        if (finalDropPercentage > 0) {
            List<DroppableSlot> mainInvPool = getMainInventorySlots(victim);
            int itemsToDropCount = (int) Math.floor(mainInvPool.size() * (finalDropPercentage / 100.0));
            addRandomSlotsToList(finalSlotsToDrop, mainInvPool, itemsToDropCount);
        }

        // --- Additional Equipment Penalty ---
        if (reputation <= Yggdrasil_ld.CONFIG.reputation_penalty_threshold) {
            List<DroppableSlot> equipmentPool = getEquipmentSlots(victim);
            equipmentPool.addAll(getTrinketSlots(victim)); // Add Trinkets to the equipment pool
            addRandomSlotsToList(finalSlotsToDrop, equipmentPool, Yggdrasil_ld.CONFIG.reputation_penalty_item_count);
        }

        // --- Dedicated Backpack Drop Logic ---
        calculateAndAddBackpackDrops(finalSlotsToDrop, victim, 0);

        dropItemsFromSlots(victim, finalSlotsToDrop);
    }

    private void calculateAndAddBackpackDrops(List<DroppableSlot> finalSlotsToDrop, PlayerEntity victim, int additionalPredatoryDrops) {
        int victimRep = ReputationManager.getReputation(victim);
        int backpacksToDrop = 0;
        net.minecraft.util.math.random.Random random = victim.getWorld().getRandom();

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

    private List<DroppableSlot> getEquipmentSlots(PlayerEntity player) {
        List<DroppableSlot> slots = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        // Armor
        for (int i = 0; i < inventory.armor.size(); i++) {
            final int slotIndex = i;
            addSlotIfNotEmpty(slots, inventory.armor.get(slotIndex), () -> inventory.armor.set(slotIndex, ItemStack.EMPTY));
        }
        // Hotbar
        for (int i = 0; i <= 8; i++) {
            final int slotIndex = i;
            addSlotIfNotEmpty(slots, inventory.main.get(slotIndex), () -> inventory.main.set(slotIndex, ItemStack.EMPTY));
        }
        // Offhand
        addSlotIfNotEmpty(slots, inventory.offHand.get(0), () -> inventory.offHand.set(0, ItemStack.EMPTY));
        return slots;
    }

    private List<DroppableSlot> getMainInventorySlots(PlayerEntity player) {
        List<DroppableSlot> slots = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        for (int i = 9; i <= 35; i++) {
            final int slotIndex = i;
            addSlotIfNotEmpty(slots, inventory.main.get(slotIndex), () -> inventory.main.set(slotIndex, ItemStack.EMPTY));
        }
        return slots;
    }

    private List<DroppableSlot> getAllEquippedBackpacks(PlayerEntity player) {
        if (FabricLoader.getInstance().isModLoaded("backpacked")) {
            return BackpackedCompat.getAllEquippedBackpacks(player);
        }
        return new ArrayList<>();
    }

    // --- NEW HELPER FOR TRINKETS ---
    private List<DroppableSlot> getTrinketSlots(PlayerEntity player) {
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

    private void dropItemsFromSlots(PlayerEntity player, List<DroppableSlot> slots) {
        for (DroppableSlot slot : slots) {
            player.dropStack(slot.stack());
            slot.clearSlotAction().run();
        }
    }
}