package net.ledok.Items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.List;

public class PercentageHealItem extends Item {

    private final float healPercentage;
    private final int useTimeTicks;
    private final int cooldownTicks;

    public PercentageHealItem(Properties settings, float healPercentage, int useTimeTicks, int cooldownTicks) {
        super(settings);
        this.healPercentage = Math.max(0.0f, Math.min(1.0f, healPercentage));
        this.useTimeTicks = useTimeTicks;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof Player player) {
            if (player.getHealth() < player.getMaxHealth()) {
                if (!world.isClientSide) {
                    float healAmount = player.getMaxHealth() * this.healPercentage;
                    player.heal(healAmount);
                    player.getCooldowns().addCooldown(this, this.cooldownTicks);
                    player.sendSystemMessage(Component.translatable("message.yggdrasil_ld.healing_potion_used").withStyle(ChatFormatting.GREEN));
                    player.playSound(SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return this.useTimeTicks;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (user.getHealth() >= user.getMaxHealth()) {
            return InteractionResultHolder.fail(itemStack);
        }
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.literal(""));

        // Prepare the placeholder values
        String healAmount = (int)(this.healPercentage * 100) + "%";
        String useTime = String.format("%.1f", this.useTimeTicks / 20.0f);
        String cooldown = String.format("%.1f", this.cooldownTicks / 20.0f);

        tooltip.add(Component.translatable("item.yggdrasil_ld.healing_potion.tooltip.heal", healAmount)
                .withStyle(ChatFormatting.BLUE));

        tooltip.add(Component.translatable("item.yggdrasil_ld.healing_potion.tooltip.use_time", useTime)
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("item.yggdrasil_ld.healing_potion.tooltip.cooldown", cooldown)
                .withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, context, tooltip, type);
    }
}
