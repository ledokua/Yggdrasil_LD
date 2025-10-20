package net.ledok.mixin;

import net.ledok.YggdrasilLdMod;
import net.ledok.reputation.ReputationManager;
import net.ledok.util.PvPContextManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void yggdrasil_handleDeathReputation(DamageSource damageSource, CallbackInfo ci) {

        if (!YggdrasilLdMod.CONFIG.reputation_change_enabled) {
            return; // If disabled, skip all reputation logic in this method.
        }

        ServerPlayer victim = (ServerPlayer) (Object) this;

        // --- GENERAL DEATH PENALTY LOGIC ---
        int currentRep = ReputationManager.getReputation(victim);
        if (currentRep < 0) {
            double penaltyPercent = YggdrasilLdMod.CONFIG.reputation_death_penalty_negative_percentage / 100.0;
            int repChange = (int) Math.floor(currentRep * penaltyPercent);
            ReputationManager.removeReputation(victim, repChange);
        } else if (currentRep > 0) {
            double penaltyPercent = YggdrasilLdMod.CONFIG.reputation_death_penalty_positive_percentage / 100.0;
            int repChange = (int) Math.floor(currentRep * penaltyPercent);
            ReputationManager.removeReputation(victim, repChange);
        }

        // --- PVP REPUTATION LOGIC ---
        // In Mojang mappings, the attacker is retrieved from the damage source
        if (damageSource.getEntity() instanceof ServerPlayer attacker && !attacker.equals(victim)) {
            PvPContextManager.setAttacker(attacker.getUUID());

            // --- ANTI-FARMING COOLDOWN CHECK ---
            if (ReputationManager.wasRecentlyKilledBy(attacker.server, attacker.getUUID(), victim.getUUID())) {
                // sendMessage is now sendSystemMessage for action bar messages
                attacker.sendSystemMessage(Component.translatable("message.yggdrasil_ld.pvp_cooldown"), true);
                return; // Exit without applying any PvP reputation changes
            }
            // Start the cooldown
            ReputationManager.recordKill(attacker.server, attacker.getUUID(), victim.getUUID());


            // --- REPUTATION CALCULATION LOGIC  ---
            int victimRep = ReputationManager.getReputation(victim);
            int attackerRep = ReputationManager.getReputation(attacker);

            boolean isAttackerPositive = attackerRep >= 100;
            boolean isAttackerNeutralOrNegative = attackerRep > -1000 && attackerRep < 100;
            boolean isAttackerDeeplyNegative = attackerRep <= -1000;
            boolean isVictimPositiveOrNeutral = victimRep >= 0;
            boolean isVictimNegative = victimRep < 0;

            if (isAttackerPositive && isVictimNegative) {
                int repLostByVictim = Math.abs(victimRep) / 2;
                ReputationManager.addReputation(victim, repLostByVictim);
                int bonusFromLoss = (int) (repLostByVictim * 0.08);
                ReputationManager.addReputation(attacker, 20 + bonusFromLoss);
            } else if (isAttackerDeeplyNegative && isVictimNegative) {
                int repStolenFromVictim = Math.abs(victimRep) / 2;
                ReputationManager.addReputation(victim, repStolenFromVictim);
                int repPenaltyForKiller = (int) (repStolenFromVictim * 0.80);
                ReputationManager.addReputation(attacker, -repPenaltyForKiller);
            } else if (isAttackerDeeplyNegative && isVictimPositiveOrNeutral) {
                int repToNeutralize = (int) (Math.abs(victimRep) * 0.08);
                ReputationManager.removeReputation(attacker, 20 + repToNeutralize);
            } else if (isAttackerNeutralOrNegative && isVictimNegative) {
                ReputationManager.addReputation(attacker, 20);
            } else {
                ReputationManager.addReputation(attacker, -100);
            }
        }
    }
}
