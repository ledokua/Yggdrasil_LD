package net.ledok.command;

import com.mojang.brigadier.CommandDispatcher;
import net.ledok.minestar.ShopCompatibility;
// MOJANG MAPPINGS: Import new classes for commands, text, and player entities.
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ShopCommand {

    // MOJANG MAPPINGS: The command source is now CommandSourceStack.
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // MOJANG MAPPINGS: CommandManager is now Commands.
        dispatcher.register(Commands.literal("shop-receive")
                .executes(context -> {
                    // MOJANG MAPPINGS: getPlayer() now returns a ServerPlayer.
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player != null) {
                        ShopCompatibility.claimPurchases(player);
                    } else {
                        // MOJANG MAPPINGS: sendError is now sendFailure, and Text is now Component.
                        context.getSource().sendFailure(Component.literal("This command can only be run by a player."));
                    }
                    return 1;
                })
        );
    }
}
