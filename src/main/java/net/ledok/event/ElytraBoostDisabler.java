package net.ledok.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.ledok.YggdrasilLdMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ElytraBoostDisabler implements UseItemCallback {
    @Override
    public InteractionResultHolder<ItemStack> interact(Player player, Level level, InteractionHand hand) {
        // Logic should only run on the server side.
        if (!level.isClientSide()) {
            ItemStack itemStack = player.getItemInHand(hand);

            // Check if the player is flying with an elytra and using a firework.
            if (player.isFallFlying() && itemStack.is(Items.FIREWORK_ROCKET)) {
                // Get the string representation of the current dimension's ID.
                String currentDimension = level.dimension().location().toString();

                // Check if the current dimension is in the configured blacklist.
                if (YggdrasilLdMod.CONFIG.elytra_boost_disabled_dimensions.contains(currentDimension)) {
                    // Send the player a message and cancel the item use event.
                    player.sendSystemMessage(Component.translatable("message.yggdrasil_ld.elytra_boost_disabled").withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack);
                }
            }
        }
        // If conditions are not met, allow the item use to proceed normally.
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
