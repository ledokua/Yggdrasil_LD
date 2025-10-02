package net.ledok.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.ledok.YggdrasilLdMod;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class AdminCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("yggdrasil_ld")
                .requires(source -> source.hasPermissionLevel(3)) // Require OP permission level for admin commands
                .then(CommandManager.literal("partial_inventory_save")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    YggdrasilLdMod.CONFIG.partial_inventory_save_enabled = enabled;

                                    String status = enabled ? "enabled" : "disabled";
                                    context.getSource().sendFeedback(() -> Text.literal("Partial inventory save feature is now " + status), true);

                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("reputation_change")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    YggdrasilLdMod.CONFIG.reputation_change_enabled = enabled;

                                    String status = enabled ? "enabled" : "disabled";
                                    context.getSource().sendFeedback(() -> Text.literal("Automatic reputation change is now " + status), true);

                                    return 1;
                                })
                        )
                )
        );

    }
}
