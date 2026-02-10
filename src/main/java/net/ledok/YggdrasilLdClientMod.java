package net.ledok;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.ledok.registry.LootBoxDefinition;
import net.ledok.registry.LootBoxRegistry;
import net.ledok.registry.ModDataComponents;

public class YggdrasilLdClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // LootBoxRegistry.initializeClient(); // Removed: Client now receives data from server
        
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex == 0) { // Only tint the first layer
                String id = stack.get(ModDataComponents.LOOT_BOX_ID);
                if (id != null) {
                    LootBoxDefinition def = LootBoxRegistry.getDefinition(id);
                    if (def != null) {
                        return def.color();
                    }
                }
            }
            return 0xFFFFFF; // Default to white (no tint)
        }, LootBoxRegistry.LOOT_BOX_ITEM);
    }
}
