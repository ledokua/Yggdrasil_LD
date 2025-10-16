package net.ledok.Items;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class RandomDripstoneItem extends Item {

    public RandomDripstoneItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof Player player) {
            if (!world.isClientSide && world instanceof ServerLevel serverLevel) {
                CommandSourceStack source = serverLevel.getServer().createCommandSourceStack();
                String command = "execute at @r run setblock ~ ~4 ~ minecraft:pointed_dripstone[vertical_direction=down]";

                serverLevel.getServer().getCommands().performPrefixedCommand(source, command);
                player.getCooldowns().addCooldown(this, 6000);
                player.playSound(SoundEvents.POINTED_DRIPSTONE_FALL, 1.0f, 1.0f);
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 100; // 5 seconds
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.yggdrasil_ld.healing_potion.tooltip.dripstone")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, type);
    }
}
