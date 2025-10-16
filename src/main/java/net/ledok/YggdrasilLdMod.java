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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YggdrasilLdMod implements ModInitializer {
    public static final String MOD_ID = "yggdrasil_ld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // MOJANG MAPPINGS: Renamed Identifier to ResourceLocation and changed constructor
    public static final ResourceLocation REPUTATION_SYNC_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "reputation_sync");

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
        // MOJANG MAPPINGS: The types for the lambda arguments (like registryAccess) have changed,
        // but type inference usually handles this. No code change is needed here if it compiles.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReputationCommand.register(dispatcher);
            AdminCommand.register(dispatcher);
            ShopCommand.register(dispatcher);
        });

        ReputationTicker.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (server.isDedicatedServer()) {
                PrimeRoleHandler.register();
            }
        });

        // --- Sync logic ---
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            server.execute(() -> {
                // MOJANG MAPPINGS: getPlayerManager() is now getPlayerList(),
                // and getPlayerList() is now getPlayers().
                // Also, the type is now ServerPlayer.
                for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
                    ReputationManager.syncReputationWithAll(server, onlinePlayer);
                }
            });
            ShopCompatibility.notifyOnJoin(handler.getPlayer());
        });
    }
}
