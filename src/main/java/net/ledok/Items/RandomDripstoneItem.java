package net.ledok.Items;

import com.mojang.brigadier.ParseResults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class RandomDripstoneItem extends Item {

    public RandomDripstoneItem(Settings settings) {
        super(settings);
    }

    /**
     * This is called when the player has held the use button for the full duration.
     * The item's effect and cooldown are applied here.
     */
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

                    // Apply a cooldown to the player (100 ticks = 5 seconds)
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

    /**
     * This sets how long the player has to hold the use button.
     * @return The use time in ticks (20 ticks = 1 second).
     */
    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 100; // 1.6 seconds
    }

    /**
     * This determines the animation played when using the item.
     * BOW is a good animation for casting or using a scroll.
     */
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    /**
     * This is called the moment the player right-clicks.
     * It starts the "using" action.
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        // This tells the game to start the "using" animation
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }
}

