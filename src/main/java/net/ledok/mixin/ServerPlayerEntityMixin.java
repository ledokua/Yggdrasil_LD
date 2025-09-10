package net.ledok.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.ledok.util.DeathItemStackManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    private static final boolean isTrinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void yggdrasil_restoreKeptItems(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (!alive) {
            ServerPlayerEntity newPlayer = (ServerPlayerEntity) (Object) this;
            DeathItemStackManager.KeptItems keptItems = DeathItemStackManager.restoreItems(oldPlayer.getUuid());

            if (keptItems != null) {
                // Повертаємо ванільні предмети
                keptItems.vanillaItems.forEach((slot, stack) -> {
                    if (!stack.isEmpty()) {
                        newPlayer.getInventory().setStack(slot, stack);
                    }
                });

                // Повертаємо предмети Trinkets у їхні слоти, якщо мод завантажено
                if (isTrinketsLoaded) {
                    TrinketsApi.getTrinketComponent(newPlayer).ifPresent(component -> {
                        for (DeathItemStackManager.TrinketInfo trinketInfo : keptItems.trinketItems) {
                            component.getInventory()
                                    .get(trinketInfo.group)
                                    .get(trinketInfo.name)
                                    .setStack(trinketInfo.index, trinketInfo.stack);
                        }
                    });
                }
            }
        }
    }
}

