package net.ledok.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ledok.networking.ModPackets;
import net.ledok.registry.LootBoxRegistry;

public class YggdrasilLdClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.ReputationSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientReputationData.setReputation(payload.playerUuid(), payload.reputation());
            });
        });
        
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SyncLootBoxesPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                LootBoxRegistry.setDefinitions(payload.definitions());
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            client.execute(ClientReputationData::clear);
        });

    }
}
