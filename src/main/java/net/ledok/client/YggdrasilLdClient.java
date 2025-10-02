package net.ledok.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ledok.networking.ModPackets;
import net.ledok.screen.BossSpawnerScreen;
import net.ledok.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class YggdrasilLdClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register the new Screen
        HandledScreens.register(ModScreenHandlers.BOSS_SPAWNER_SCREEN_HANDLER, BossSpawnerScreen::new);

        // Packet handler
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.ReputationSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientReputationData.setReputation(payload.playerUuid(), payload.reputation());
            });
        });

        // On disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            client.execute(ClientReputationData::clear);
        });
    }
}
