package net.ledok.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ledok.networking.ModPackets;
import net.ledok.registry.BlockRegistry;
import net.ledok.screen.BossSpawnerScreen;
import net.ledok.screen.MobAttributesScreen;
import net.ledok.screen.MobSpawnerScreen;
import net.ledok.screen.ModScreenHandlers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;

public class YggdrasilLdClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.BOSS_SPAWNER_SCREEN_HANDLER, BossSpawnerScreen::new);
        MenuScreens.register(ModScreenHandlers.MOB_SPAWNER_SCREEN_HANDLER, MobSpawnerScreen::new);
        MenuScreens.register(ModScreenHandlers.MOB_ATTRIBUTES_SCREEN_HANDLER, MobAttributesScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.ReputationSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ClientReputationData.setReputation(payload.playerUuid(), payload.reputation());
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            client.execute(ClientReputationData::clear);
        });

        BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.PHASE_BLOCK, RenderType.translucent());
    }
}
