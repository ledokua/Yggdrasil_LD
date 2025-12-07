package net.ledok.client.renderer.armor;

import net.ledok.Items.PriestT6ArmorItem;
import net.ledok.YggdrasilLdMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class PriestT6ArmorRenderer extends GeoArmorRenderer<PriestT6ArmorItem> {
    public PriestT6ArmorRenderer() {
        super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(YggdrasilLdMod.MOD_ID, "armor/priest_t6_armor")));
    }
}
