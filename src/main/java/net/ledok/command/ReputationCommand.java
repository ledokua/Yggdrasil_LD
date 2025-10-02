package net.ledok.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.ledok.YggdrasilLdMod;
import net.ledok.reputation.ReputationManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.stream.Collectors;

public class ReputationCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reputation")
                .then(CommandManager.literal("get")
                        .executes(context -> checkReputation(context.getSource(), context.getSource().getPlayer()))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> checkReputation(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                        )
                )
                .then(CommandManager.literal("add")
                        .requires(source -> source.hasPermissionLevel(2)) // OP only
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
                        .requires(source -> source.hasPermissionLevel(2)) // OP only
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
                .then(CommandManager.literal("set")
                        .requires(source -> source.hasPermissionLevel(2)) // OP only
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> setReputation(
                                                context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        ))
                                )
                        )
                )
                .then(CommandManager.literal("top")
                        .executes(context -> showTopReputation(context.getSource()))
                )
                .then(CommandManager.literal("bottom")
                        .executes(context -> showBottomReputation(context.getSource()))
                )
                .then(CommandManager.literal("tozero")
                        .requires(source -> source.hasPermissionLevel(2)) // OP only
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("percentage", IntegerArgumentType.integer(0, 100))
                                        .executes(context -> bringReputationToZero(
                                                context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "percentage")
                                        ))
                                )
                        )
                )
        );
    }

    private static int bringReputationToZero(ServerCommandSource source, ServerPlayerEntity player, int percentage) {
        int currentRep = ReputationManager.getReputation(player);
        if (currentRep == 0) {
            source.sendFeedback(() -> Text.translatable("command.yggdrasil_ld.tozero.already_zero", player.getName().getString()), true);
            return 1;
        }

        // Calculate the reduction amount
        double reduction = currentRep * (percentage / 100.0);
        // Apply the reduction. The result will naturally move towards zero.
        int newRep = (int) Math.round(currentRep - reduction);

        ReputationManager.setReputation(player, newRep);

        source.sendFeedback(() -> Text.translatable("command.yggdrasil_ld.tozero.success", player.getName().getString(), percentage, newRep), true);
        return 1;
    }

    private static int showTopReputation(ServerCommandSource source) {
        Map<UUID, Integer> allReputations = ReputationManager.getAllReputations(source.getServer());

        List<Map.Entry<UUID, Integer>> sortedList = allReputations.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(YggdrasilLdMod.CONFIG.leaderboard_size)
                .collect(Collectors.toList());

        displayLeaderboard(source, sortedList, Text.translatable("command.yggdrasil_ld.leaderboard.top_title"), Formatting.GREEN);
        return 1;
    }

    private static int showBottomReputation(ServerCommandSource source) {
        Map<UUID, Integer> allReputations = ReputationManager.getAllReputations(source.getServer());

        List<Map.Entry<UUID, Integer>> sortedList = allReputations.entrySet().stream()
                .filter(entry -> entry.getValue() < 0)
                .sorted(Map.Entry.comparingByValue()) // Default is ascending (most negative first)
                .limit(YggdrasilLdMod.CONFIG.leaderboard_size)
                .collect(Collectors.toList());

        displayLeaderboard(source, sortedList, Text.translatable("command.yggdrasil_ld.leaderboard.bottom_title"), Formatting.RED);
        return 1;
    }

    private static void displayLeaderboard(ServerCommandSource source, List<Map.Entry<UUID, Integer>> sortedList, Text title, Formatting color) {
        source.sendFeedback(() -> title, false);

        if (sortedList.isEmpty()) {
            source.sendFeedback(() -> Text.translatable("command.yggdrasil_ld.leaderboard.empty"), false);
            return;
        }

        MinecraftServer server = source.getServer();
        for (int i = 0; i < sortedList.size(); i++) {
            Map.Entry<UUID, Integer> entry = sortedList.get(i);
            UUID playerUuid = entry.getKey();
            int reputation = entry.getValue();

            Optional<GameProfile> profileOpt = server.getUserCache().getByUuid(playerUuid);
            String playerName = profileOpt.map(GameProfile::getName).orElse(playerUuid.toString().substring(0, 8));

            MutableText line = Text.literal((i + 1) + ". " + playerName + ": ")
                    .append(Text.literal(String.valueOf(reputation)).formatted(color));

            source.sendFeedback(() -> line, false);
        }
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

    private static int setReputation(ServerCommandSource source, ServerPlayerEntity player, int amount) {
        ReputationManager.setReputation(player, amount);
        int newReputation = ReputationManager.getReputation(player);
        source.sendFeedback(() -> Text.literal("Reputation set " + amount + " for " + player.getName().getString() + ". New reputation: " + newReputation), true);
        return 1;
    }
}

