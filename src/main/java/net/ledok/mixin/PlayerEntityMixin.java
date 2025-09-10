package net.ledok.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.ledok.Yggdrasil_ld;
import net.ledok.util.DeathItemStackManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow public abstract PlayerInventory getInventory();

    private static final boolean isTrinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");

    @Inject(method = "dropInventory", at = @At("HEAD"))
    private void yggdrasil_partialKeepInventory(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerInventory inventory = this.getInventory();

        if (player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || Yggdrasil_ld.CONFIG.keep_inventory_drop_percentage >= 100.0) {
            return;
        }

        Map<Integer, ItemStack> vanillaItemsToKeep = new HashMap<>();
        List<Integer> mainInventorySlots = new ArrayList<>();
        List<DeathItemStackManager.TrinketInfo> trinketsToKeep = new ArrayList<>();

        // Розрахунок для основного інвентарю (без змін)
        for (int i = 9; i < 36; i++) {
            if (!inventory.getStack(i).isEmpty()) { mainInventorySlots.add(i); }
        }
        double keepPercentage = 1.0 - (Yggdrasil_ld.CONFIG.keep_inventory_drop_percentage / 100.0);
        int itemsToKeepFromMainCount = (int) Math.floor(mainInventorySlots.size() * keepPercentage);
        Collections.shuffle(mainInventorySlots);
        List<Integer> slotsToKeepFromMain = new ArrayList<>();
        if (itemsToKeepFromMainCount > 0) {
            slotsToKeepFromMain.addAll(mainInventorySlots.subList(0, itemsToKeepFromMainCount));
        }

        // Наповнення Map ванільними предметами (без змін)
        for (int i = 0; i < 9; i++) vanillaItemsToKeep.put(i, inventory.getStack(i).copy());
        for (int i = 0; i < inventory.armor.size(); i++) vanillaItemsToKeep.put(36 + i, inventory.armor.get(i).copy());
        vanillaItemsToKeep.put(PlayerInventory.OFF_HAND_SLOT, inventory.offHand.get(0).copy());
        for (int slot : slotsToKeepFromMain) vanillaItemsToKeep.put(slot, inventory.getStack(slot).copy());

        // Обробка Trinkets
        if (isTrinketsLoaded) {
            TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
                component.getAllEquipped().forEach(pair -> {
                    var slotReference = pair.getLeft();
                    var itemStack = pair.getRight();
                    if (!itemStack.isEmpty()) {
                        var slotType = slotReference.inventory().getSlotType();
                        // Створюємо та зберігаємо повну інформацію про Trinket
                        trinketsToKeep.add(new DeathItemStackManager.TrinketInfo(
                                slotType.getGroup(),
                                slotType.getName(),
                                slotReference.index(),
                                itemStack.copy()
                        ));
                        // Очищуємо слот
                        slotReference.inventory().setStack(slotReference.index(), ItemStack.EMPTY);
                    }
                });
            });
        }

        // Зберігаємо обидва типи предметів
        DeathItemStackManager.keepItems(player.getUuid(), vanillaItemsToKeep, trinketsToKeep);

        // Очищуємо збережені ванільні слоти
        for (Integer slotIndex : vanillaItemsToKeep.keySet()) {
            inventory.setStack(slotIndex, ItemStack.EMPTY);
        }
    }
}

