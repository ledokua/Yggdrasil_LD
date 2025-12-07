package net.ledok;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ledok.command.AdminCommand;
import net.ledok.command.ReputationCommand;
import net.ledok.command.ShopCommand;
import net.ledok.config.ModConfigs;
import net.ledok.event.ElytraBoostDisabler;
import net.ledok.event.ReputationTicker;
import net.ledok.minestar.ShopCompatibility;
import net.ledok.networking.ModPackets;
import net.ledok.prime.PrimeRoleHandler;
import net.ledok.registry.*;
import net.ledok.reputation.ReputationManager;
import net.ledok.screen.ModScreenHandlers;
import net.ledok.util.BossDataComponent;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YggdrasilLdMod implements ModInitializer {
    public static final String MOD_ID = "yggdrasil_ld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfigs CONFIG;

    @Override
    public void onInitialize() {
        LOGGER.info("Yggdrasil LD has been initialized!");
        CONFIG = ModConfigs.load();
        ArmorMaterialRegistry.initialize();
        ItemRegistry.initialize();
        ArmorRegistry.initialize();
        BlockRegistry.initialize();
        BlockEntitiesRegistry.initialize();
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
            if (server.isDedicatedServer()) {
                PrimeRoleHandler.register();
            }
        });

        // --- Sync logic ---
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            server.execute(() -> {
                for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
                    ReputationManager.syncReputationWithAll(server, onlinePlayer);
                }
            });
            ShopCompatibility.notifyOnJoin(handler.getPlayer());
        });
    }
}
