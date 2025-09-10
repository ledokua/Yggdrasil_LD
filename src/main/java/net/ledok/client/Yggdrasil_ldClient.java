package net.ledok.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.ledok.networking.ModPacketsClient;

public class Yggdrasil_ldClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Реєструємо наші клієнтські обробники пакетів
        ModPacketsClient.registerS2CPackets();

        // Використовуємо новий, правильний спосіб для обробки виходу з серверу
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            client.execute(ClientReputationData::clear);
        });
    }
}