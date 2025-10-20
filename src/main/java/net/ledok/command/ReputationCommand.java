package net.ledok.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.ledok.YggdrasilLdMod;
import net.ledok.reputation.ReputationManager;
// MOJANG MAPPINGS: Update all imports to their new locations and names.
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;

import java.util.*;
import java.util.stream.Collectors;

public class ReputationCommand {

    // MOJANG MAPPINGS: The command source is now CommandSourceStack.
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reputation")
                .then(Commands.literal("get")
                        .executes(context -> checkReputation(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                // MOJANG MAPPINGS: EntityArgumentType is now EntityArgument.
                                .executes(context -> checkReputation(context.getSource(), EntityArgument.getPlayer(context, "player")))
                        )
                )
                .then(Commands.literal("add")
                        .requires(source -> source.hasPermission(2)) // OP only
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> addReputation(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        ))
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2)) // OP only
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> removeReputation(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        ))
                                )
                        )
                )
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2)) // OP only
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> setReputation(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        ))
                                )
                        )
                )
                .then(Commands.literal("top")
                        .executes(context -> showTopReputation(context.getSource()))
                )
                .then(Commands.literal("bottom")
                        .executes(context -> showBottomReputation(context.getSource()))
                )
                .then(Commands.literal("tozero")
                        .requires(source -> source.hasPermission(2)) // OP only
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("percentage", IntegerArgumentType.integer(0, 100))
                                        .executes(context -> bringReputationToZero(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "percentage")
                                        ))
                                )
                        )
                )
        );
    }

    // MOJANG MAPPINGS: Update method signatures to use new types.
    private static int bringReputationToZero(CommandSourceStack source, ServerPlayer player, int percentage) {
        int currentRep = ReputationManager.getReputation(player);
        if (currentRep == 0) {
            source.sendSuccess(() -> Component.translatable("command.yggdrasil_ld.tozero.already_zero", player.getName().getString()), true);
            return 1;
        }

        double reduction = currentRep * (percentage / 100.0);
        int newRep = (int) Math.round(currentRep - reduction);

        ReputationManager.setReputation(player, newRep);

        source.sendSuccess(() -> Component.translatable("command.yggdrasil_ld.tozero.success", player.getName().getString(), percentage, newRep), true);
        return 1;
    }

    private static int showTopReputation(CommandSourceStack source) {
        Map<UUID, Integer> allReputations = ReputationManager.getAllReputations(source.getServer());

        List<Map.Entry<UUID, Integer>> sortedList = allReputations.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(YggdrasilLdMod.CONFIG.leaderboard_size)
                .collect(Collectors.toList());

        displayLeaderboard(source, sortedList, Component.translatable("command.yggdrasil_ld.leaderboard.top_title"), ChatFormatting.GREEN);
        return 1;
    }

    private static int showBottomReputation(CommandSourceStack source) {
        Map<UUID, Integer> allReputations = ReputationManager.getAllReputations(source.getServer());

        List<Map.Entry<UUID, Integer>> sortedList = allReputations.entrySet().stream()
                .filter(entry -> entry.getValue() < 0)
                .sorted(Map.Entry.comparingByValue())
                .limit(YggdrasilLdMod.CONFIG.leaderboard_size)
                .collect(Collectors.toList());

        displayLeaderboard(source, sortedList, Component.translatable("command.yggdrasil_ld.leaderboard.bottom_title"), ChatFormatting.RED);
        return 1;
    }

    private static void displayLeaderboard(CommandSourceStack source, List<Map.Entry<UUID, Integer>> sortedList, Component title, ChatFormatting color) {
        source.sendSuccess(() -> title, false);

        if (sortedList.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.yggdrasil_ld.leaderboard.empty"), false);
            return;
        }

        MinecraftServer server = source.getServer();
        for (int i = 0; i < sortedList.size(); i++) {
            Map.Entry<UUID, Integer> entry = sortedList.get(i);
            UUID playerUuid = entry.getKey();
            int reputation = entry.getValue();

            // MOJANG MAPPINGS: getUserCache() is now getProfileCache(), and getByUuid is now get().
            Optional<GameProfile> profileOpt = server.getProfileCache().get(playerUuid);
            String playerName = profileOpt.map(GameProfile::getName).orElse(playerUuid.toString().substring(0, 8));

            MutableComponent line = Component.literal((i + 1) + ". " + playerName + ": ")
                    .append(Component.literal(String.valueOf(reputation)).withStyle(color));

            source.sendSuccess(() -> line, false);
        }
    }

    private static int checkReputation(CommandSourceStack source, ServerPlayer player) {
        int reputation = ReputationManager.getReputation(player);
        source.sendSuccess(() -> Component.literal("Reputation for " + player.getName().getString() + " is: " + reputation), false);
        return 1;
    }

    private static int addReputation(CommandSourceStack source, ServerPlayer player, int amount) {
        ReputationManager.addReputation(player, amount);
        int newReputation = ReputationManager.getReputation(player);
        source.sendSuccess(() -> Component.literal("Added " + amount + " reputation to " + player.getName().getString() + ". New reputation: " + newReputation), true);
        return 1;
    }

    private static int removeReputation(CommandSourceStack source, ServerPlayer player, int amount) {
        ReputationManager.removeReputation(player, amount);
        int newReputation = ReputationManager.getReputation(player);
        source.sendSuccess(() -> Component.literal("Removed " + amount + " reputation from " + player.getName().getString() + ". New reputation: " + newReputation), true);
        return 1;
    }

    private static int setReputation(CommandSourceStack source, ServerPlayer player, int amount) {
        ReputationManager.setReputation(player, amount);
        int newReputation = ReputationManager.getReputation(player);
        source.sendSuccess(() -> Component.literal("Reputation set " + amount + " for " + player.getName().getString() + ". New reputation: " + newReputation), true);
        return 1;
    }
}
