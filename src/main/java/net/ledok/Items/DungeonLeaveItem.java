package net.ledok.Items;

import com.mojang.brigadier.ParseResults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DungeonLeaveItem extends Item {

    public DungeonLeaveItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            MinecraftServer server = world.getServer();
            if (server == null) {
                return TypedActionResult.fail(user.getStackInHand(hand));
            }

            ServerCommandSource source = server.getCommandSource();

            String playerName = user.getName().getString();
            String command = String.format("execute as %s run dungeon leave", playerName);

            // ---------------------

            CommandManager commandManager = server.getCommandManager();
            ParseResults<ServerCommandSource> parseResults = commandManager.getDispatcher().parse(command, source.withSilent());
            commandManager.execute(parseResults, command);

            if (!user.getAbilities().creativeMode) {
                user.getStackInHand(hand).decrement(1);
            }

            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}