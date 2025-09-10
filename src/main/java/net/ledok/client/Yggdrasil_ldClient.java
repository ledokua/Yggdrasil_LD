package net.ledok.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ledok.networking.ModPackets;

public class Yggdrasil_ldClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Реєструємо обробник для нашого пакету
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.ReputationSyncPayload.ID, (payload, context) -> {
            // Виконуємо оновлення в головному потоці клієнта, щоб уникнути помилок
            context.client().execute(() -> {
                ClientReputationData.setReputation(payload.playerUuid(), payload.reputation());
            });
        });

        // Використовуємо новий, правильний спосіб для обробки виходу з серверу
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            client.execute(ClientReputationData::clear);
        });
    }
}

