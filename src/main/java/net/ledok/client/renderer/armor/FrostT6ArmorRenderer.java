package net.ledok.client.renderer.armor;

import net.ledok.Items.FrostT6ArmorItem;
import net.ledok.YggdrasilLdMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

    public final class FrostT6ArmorRenderer extends GeoArmorRenderer<FrostT6ArmorItem> {
        public FrostT6ArmorRenderer() {
            super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "armor/frost_t6_armor")));
        }
    }