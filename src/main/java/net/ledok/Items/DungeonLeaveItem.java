package net.ledok.Items;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DungeonLeaveItem extends Item {

    public DungeonLeaveItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide && world instanceof ServerLevel serverLevel) {
            CommandSourceStack source = serverLevel.getServer().createCommandSourceStack();
            String playerName = user.getGameProfile().getName();
            String command = String.format("execute as %s run dungeon leave", playerName);

            serverLevel.getServer().getCommands().performPrefixedCommand(source, command);

            if (!user.getAbilities().instabuild) {
                user.getItemInHand(hand).shrink(1);
            }

            return InteractionResultHolder.success(user.getItemInHand(hand));
        }
        return InteractionResultHolder.pass(user.getItemInHand(hand));
    }
}
