package net.ledok.Items;

import com.mojang.brigadier.ParseResults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;

public class RandomDripstoneItem extends Item {

    public RandomDripstoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            if (!world.isClient) {
                MinecraftServer server = world.getServer();
                if (server != null) {
                    ServerCommandSource source = server.getCommandSource();
                    String command = "execute at @r if block ~ ~4 ~ air if block ~ ~5 ~ air run setblock ~ ~4 ~ minecraft:pointed_dripstone[vertical_direction=down ]";

                    // Execute the command
                    CommandManager commandManager = server.getCommandManager();
                    ParseResults<ServerCommandSource> parseResults = commandManager.getDispatcher().parse(command, source.withSilent());
                    commandManager.execute(parseResults, command);
                    player.getItemCooldownManager().set(this, 6000);
                    // Play a sound effect for feedback
                    player.playSound(SoundEvents.BLOCK_POINTED_DRIPSTONE_FALL, 1.0f, 1.0f);
                }
            }

            // Consume one of the scrolls if the player is not in creative mode
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 100; // 1.6 seconds
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        // This tells the game to start the "using" animation
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal(""));

        tooltip.add(Text.translatable("item.yggdrasil_ld.healing_potion.tooltip.dripstone")
                .formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}

