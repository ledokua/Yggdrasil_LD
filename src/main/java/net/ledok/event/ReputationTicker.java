package net.ledok.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.ledok.YggdrasilLdMod;
import net.ledok.reputation.ReputationManager;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles the global timer for passively awarding reputation to online players.
 */
public class ReputationTicker {

    private static int timer = 0;
    private static final int TICKS_PER_MINUTE = 1200; // 20 ticks/second * 60 seconds

    public static void register() {
        // Do not register the event if the feature is disabled in the config.
        if (!YggdrasilLdMod.CONFIG.passive_reputation_enabled) {
            return;
        }

        // Register a listener that fires at the end of every server tick.
        ServerTickEvents.END_SERVER_TICK.register(server -> {

            if (!YggdrasilLdMod.CONFIG.reputation_change_enabled) {
                return; // Do nothing if the feature is disabled by an admin.
            }

            timer++;

            // Calculate the target number of ticks based on the config.
            int intervalInTicks = YggdrasilLdMod.CONFIG.passive_reputation_interval_minutes * TICKS_PER_MINUTE;

            if (timer >= intervalInTicks) {
                timer = 0; // Reset the timer.

                int reputationAmount = YggdrasilLdMod.CONFIG.passive_reputation_amount;

                // Loop through every online player and give them the reputation.
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    ReputationManager.addReputation(player, reputationAmount);
                }
            }
        });
    }
}
