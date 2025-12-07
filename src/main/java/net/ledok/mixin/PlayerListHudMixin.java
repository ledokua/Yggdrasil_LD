package net.ledok.mixin;

import net.ledok.client.ClientReputationData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class PlayerListHudMixin {

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void addReputationToPlayerName(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
        // Get player name
        Component originalName = cir.getReturnValue();
        // Get player reputation
        int reputation = ClientReputationData.getReputation(entry.getProfile().getId());

        MutableComponent reputationText = Component.literal(" [" + reputation + "]");

        // Color in playerlist (TAB)
        if (reputation > 0) {
            reputationText.withStyle(ChatFormatting.GREEN);
        } else if (reputation < 0) {
            reputationText.withStyle(ChatFormatting.RED);
        } else {
            reputationText.withStyle(ChatFormatting.GRAY);
        }

        // Add text to back of the player name
        cir.setReturnValue(originalName.copy().append(reputationText));
    }
}
