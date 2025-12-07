package net.ledok.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.ledok.YggdrasilLdMod;
// MOJANG MAPPINGS: Import new classes for commands and text components.
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class AdminCommand {

    // MOJANG MAPPINGS: The command source is now CommandSourceStack.
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // MOJANG MAPPINGS: CommandManager is now Commands.
        dispatcher.register(Commands.literal("yggdrasil_ld")
                .requires(source -> source.hasPermission(3)) // Require OP permission level for admin commands
                .then(Commands.literal("partial_inventory_save")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    YggdrasilLdMod.CONFIG.partial_inventory_save_enabled = enabled;

                                    String status = enabled ? "enabled" : "disabled";
                                    // MOJANG MAPPINGS: sendFeedback is now sendSuccess, and Text is now Component.
                                    context.getSource().sendSuccess(() -> Component.literal("Partial inventory save feature is now " + status), true);

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("reputation_change")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    YggdrasilLdMod.CONFIG.reputation_change_enabled = enabled;

                                    String status = enabled ? "enabled" : "disabled";
                                    context.getSource().sendSuccess(() -> Component.literal("Automatic reputation change is now " + status), true);

                                    return 1;
                                })
                        )
                )
        );
    }
}
