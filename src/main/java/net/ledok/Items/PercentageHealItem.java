package net.ledok.Items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class PercentageHealItem extends Item {

    private final float healPercentage;
    private final int useTimeTicks;
    private final int cooldownTicks;

    public PercentageHealItem(Settings settings, float healPercentage, int useTimeTicks, int cooldownTicks) {
        super(settings);
        this.healPercentage = Math.max(0.0f, Math.min(1.0f, healPercentage));
        this.useTimeTicks = useTimeTicks;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            if (player.getHealth() < player.getMaxHealth()) {
                if (!world.isClient) {
                    float healAmount = player.getMaxHealth() * this.healPercentage;
                    player.heal(healAmount);
                    player.getItemCooldownManager().set(this, this.cooldownTicks);
                    player.sendMessage(Text.translatable("message.yggdrasil_ld.healing_potion_used").formatted(Formatting.GREEN), true);
                    player.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                }
            }
        }
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return this.useTimeTicks;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user.getHealth() >= user.getMaxHealth()) {
            return TypedActionResult.fail(itemStack);
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }
}

