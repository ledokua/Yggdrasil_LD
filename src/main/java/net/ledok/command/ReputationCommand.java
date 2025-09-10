package net.ledok.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.ledok.reputation.ReputationManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ReputationCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reputation")
                .then(CommandManager.literal("check")
                        .executes(context -> checkReputation(context.getSource(), context.getSource().getPlayer()))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> checkReputation(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                        )
                )
                .then(CommandManager.literal("add")
                        .requires(source -> source.hasPermissionLevel(2)) // Тільки для операторів
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> addReputation(
                                                context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        ))
                                )
                        )
                )
                .then(CommandManager.literal("remove")
                        .requires(source -> source.hasPermissionLevel(2)) // Тільки для операторів
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> removeReputation(
                                                context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        ))
                                )
                        )
                )
        );
    }

    private static int checkReputation(ServerCommandSource source, ServerPlayerEntity player) {
        int reputation = ReputationManager.getReputation(player);
        source.sendFeedback(() -> Text.literal("Reputation for " + player.getName().getString() + " is: " + reputation), false);
        return 1;
    }

    private static int addReputation(ServerCommandSource source, ServerPlayerEntity player, int amount) {
        ReputationManager.addReputation(player, amount);
        int newReputation = ReputationManager.getReputation(player);
        source.sendFeedback(() -> Text.literal("Added " + amount + " reputation to " + player.getName().getString() + ". New reputation: " + newReputation), true);
        return 1;
    }

    private static int removeReputation(ServerCommandSource source, ServerPlayerEntity player, int amount) {
        ReputationManager.removeReputation(player, amount);
        int newReputation = ReputationManager.getReputation(player);
        source.sendFeedback(() -> Text.literal("Removed " + amount + " reputation from " + player.getName().getString() + ". New reputation: " + newReputation), true);
        return 1;
    }
}
