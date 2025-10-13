package net.ledok.command;

import com.mojang.brigadier.CommandDispatcher;
import net.ledok.minestar.ShopCompatibility;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ShopCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("shop-receive")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        ShopCompatibility.claimPurchases(player);
                    } else {
                        context.getSource().sendError(Text.literal("This command can only be run by a player."));
                    }
                    return 1;
                })
        );
    }
}