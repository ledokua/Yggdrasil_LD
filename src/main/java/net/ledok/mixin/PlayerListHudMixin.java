package net.ledok.mixin;

import net.ledok.client.ClientReputationData;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void addReputationToPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        // Get pl name
        Text originalName = cir.getReturnValue();
        // Get pl reputation
        int reputation = ClientReputationData.getReputation(entry.getProfile().getId());

        MutableText reputationText = Text.literal(" [" + reputation + "]");

        // Color
        if (reputation > 0) {
            reputationText.formatted(Formatting.GREEN);
        } else if (reputation < 0) {
            reputationText.formatted(Formatting.RED);
        } else {
            reputationText.formatted(Formatting.GRAY);
        }

        // Add text to back
        cir.setReturnValue(originalName.copy().append(reputationText));
    }
}
