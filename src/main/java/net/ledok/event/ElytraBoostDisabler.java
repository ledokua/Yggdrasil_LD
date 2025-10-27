package net.ledok.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.ledok.YggdrasilLdMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attributes; // Import Attributes
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
                    // Check if the armor threshold feature is enabled
                    if (YggdrasilLdMod.CONFIG.elytra_armor_threshold_enabled) {
                        // Get the player's current armor value
                        double armorValue = player.getAttributeValue(Attributes.ARMOR);
                        // If armor is below the threshold, allow boosting
                        if (armorValue < YggdrasilLdMod.CONFIG.elytra_armor_threshold) {
                            return InteractionResultHolder.pass(itemStack); // Allow boosting
                        }
                    }

                    // If armor check is disabled OR armor is >= threshold, disable boosting
                    player.sendSystemMessage(Component.translatable("message.yggdrasil_ld.elytra_boost_disabled").withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack); // Disable boosting
                }
            }
        }
        // If conditions are not met (not flying, not firework, or not in blacklisted dimension), allow the item use to proceed normally.
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}