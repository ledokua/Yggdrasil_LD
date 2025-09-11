package net.ledok.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ledok.networking.ModPackets;

public class Yggdrasil_ldClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Packet handler
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.ReputationSyncPayload.ID, (payload, context) -> {
            // Update on client side
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

