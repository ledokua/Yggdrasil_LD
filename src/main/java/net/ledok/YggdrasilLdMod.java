package net.ledok;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ledok.Items.Items;
import net.ledok.block.ModBlocks;
import net.ledok.block.entity.ModBlockEntities;
import net.ledok.command.AdminCommand;
import net.ledok.command.ReputationCommand;
import net.ledok.command.ShopCommand;
import net.ledok.config.ModConfigs;
import net.ledok.event.ElytraBoostDisabler;
import net.ledok.event.ReputationTicker;
import net.ledok.minestar.ShopCompatibility;
import net.ledok.networking.ModPackets;
import net.ledok.prime.PrimeRoleHandler;
import net.ledok.reputation.ReputationManager;
import net.ledok.screen.ModScreenHandlers;
import net.ledok.util.BossDataComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YggdrasilLdMod implements ModInitializer {
    public static final String MOD_ID = "yggdrasil_ld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier REPUTATION_SYNC_ID = Identifier.of(MOD_ID, "reputation_sync");

    public static ModConfigs CONFIG;

    @Override
    public void onInitialize() {
        LOGGER.info("Yggdrasil LD has been initialized!");
        Items.initialize();
        CONFIG = ModConfigs.load();

        ModBlocks.initialize();
        ModBlockEntities.initialize();
        ModScreenHandlers.initialize();
        ModPackets.registerC2SPackets();
        ModPackets.registerS2CPackets();
        BossDataComponent.initialize();

        UseItemCallback.EVENT.register(new ElytraBoostDisabler());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReputationCommand.register(dispatcher);
            AdminCommand.register(dispatcher);
            ShopCommand.register(dispatcher);
        });

        ReputationTicker.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (server.isDedicated()) {
                PrimeRoleHandler.register();
            }
        });

        // --- Sync logic ---
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            server.execute(() -> {
                for (ServerPlayerEntity onlinePlayer : server.getPlayerManager().getPlayerList()) {
                    ReputationManager.syncReputationWithAll(server, onlinePlayer);
                }
            });
            ShopCompatibility.notifyOnJoin(handler.getPlayer());
        });
    }
}
