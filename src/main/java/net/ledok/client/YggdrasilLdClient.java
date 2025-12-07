package net.ledok.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ledok.networking.ModPackets;
import net.ledok.screen.BossSpawnerScreen;
import net.ledok.screen.MobSpawnerScreen;
import net.ledok.screen.ModScreenHandlers;
import net.minecraft.client.gui.screens.MenuScreens;

public class YggdrasilLdClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register the screens with the new MenuScreens class.
        MenuScreens.register(ModScreenHandlers.BOSS_SPAWNER_SCREEN_HANDLER, BossSpawnerScreen::new);
        MenuScreens.register(ModScreenHandlers.MOB_SPAWNER_SCREEN_HANDLER, MobSpawnerScreen::new);

        // Register the packet handler with the updated payload type.
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.ReputationSyncPayload.TYPE, (payload, context) -> {
            // The logic remains the same, but the context and payload now use Mojang-mapped types.
            context.client().execute(() -> {
                ClientReputationData.setReputation(payload.playerUuid(), payload.reputation());
            });
        });

        // Register the disconnect event.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            client.execute(ClientReputationData::clear);
        });
    }
}
