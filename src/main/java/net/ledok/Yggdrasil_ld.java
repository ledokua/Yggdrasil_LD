package net.ledok;

import net.fabricmc.api.ModInitializer;
import net.ledok.Items.Items;

public class Yggdrasil_ld implements ModInitializer {

    public static final String MOD_ID = "yggdrasil_ld";

    @Override
    public void onInitialize() {
        Items.initialize();
    }
}
