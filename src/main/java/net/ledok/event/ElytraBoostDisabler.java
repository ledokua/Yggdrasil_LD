package net.ledok.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.ledok.YggdrasilLdMod;
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
        // ONLY SERVER LOGIC!!!!
        if (!world.isClient) {
            ItemStack itemStack = player.getStackInHand(hand);

            // CONDITIONS:
            // Using elytra
            // Has firework in hand
            if (player.isFallFlying() && itemStack.isOf(Items.FIREWORK_ROCKET)) {
                // Check dimension ID
                String currentDimension = world.getRegistryKey().getValue().toString();

                // Is blacklisted?
                if (YggdrasilLdMod.CONFIG.elytra_boost_disabled_dimensions.contains(currentDimension)) {
                    // Send player message if no boos allowed and cancel event
                    player.sendMessage(Text.translatable("message.yggdrasil_ld.elytra_boost_disabled").formatted(Formatting.RED), true);
                    return TypedActionResult.fail(itemStack);
                }
            }
        }
        // If all ok allow boost
        return TypedActionResult.pass(player.getStackInHand(hand));
    }
}
