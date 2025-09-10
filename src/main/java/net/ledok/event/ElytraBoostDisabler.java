package net.ledok.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.ledok.Yggdrasil_ld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ElytraBoostDisabler implements UseItemCallback {
    @Override
    public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
        // Виконуємо логіку тільки на сервері
        if (!world.isClient) {
            ItemStack itemStack = player.getStackInHand(hand);

            // Перевіряємо умови:
            // 1. Гравець летить на елітрах
            // 2. Предмет в руці - феєрверк
            if (player.isFallFlying() && itemStack.isOf(Items.FIREWORK_ROCKET)) {
                // Отримуємо ID поточного виміру
                String currentDimension = world.getRegistryKey().getValue().toString();

                // Перевіряємо, чи є цей вимір у списку заборонених
                if (Yggdrasil_ld.CONFIG.elytra_boost_disabled_dimensions.contains(currentDimension)) {
                    // Надсилаємо гравцю повідомлення
                    player.sendMessage(Text.translatable("message.yggdrasil_ld.elytra_boost_disabled").formatted(Formatting.RED), true);
                    // Скасовуємо подію, щоб феєрверк не використався
                    return TypedActionResult.fail(itemStack);
                }
            }
        }
        // Якщо умови не виконані, дозволяємо стандартну поведінку
        return TypedActionResult.pass(player.getStackInHand(hand));
    }
}
