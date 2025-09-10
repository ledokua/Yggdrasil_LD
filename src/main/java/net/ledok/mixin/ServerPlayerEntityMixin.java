package net.ledok.mixin;

import net.ledok.Yggdrasil_ld;
import net.ledok.reputation.ReputationManager;
import net.ledok.util.PvPContextManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void yggdrasil_handleDeathReputation(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity victim = (ServerPlayerEntity) (Object) this;

        // --- ЛОГІКА ВТРАТИ РЕПУТАЦІЇ ПРИ СМЕРТІ (ВІДСОТКОВА) ---
        int currentRep = ReputationManager.getReputation(victim);
        if (currentRep < 0) {
            double penaltyPercent = Yggdrasil_ld.CONFIG.reputation_death_penalty_negative_percentage / 100.0;
            int repChange = (int) Math.floor(currentRep * penaltyPercent);
            ReputationManager.removeReputation(victim, repChange); // Віднімаємо від'ємне число, що збільшує репутацію
        } else if (currentRep > 0) {
            double penaltyPercent = Yggdrasil_ld.CONFIG.reputation_death_penalty_positive_percentage / 100.0;
            int repChange = (int) Math.floor(currentRep * penaltyPercent);
            ReputationManager.removeReputation(victim, repChange); // Віднімаємо додатнє число, що зменшує репутацію
        }


        LivingEntity attackerEntity = victim.getAttacker();
        if (attackerEntity instanceof ServerPlayerEntity attacker && !attacker.equals(victim)) {
            PvPContextManager.setAttacker(attacker.getUuid());

            int victimRep = ReputationManager.getReputation(victim);
            int attackerRep = ReputationManager.getReputation(attacker);

            boolean bountyEnabled = Yggdrasil_ld.CONFIG.bounty_hunter_kill_enabled;
            boolean stealingEnabled = Yggdrasil_ld.CONFIG.reputation_stealing_enabled;
            boolean antiAbuseEnabled = Yggdrasil_ld.CONFIG.anti_abuse_kill_enabled;

            // 1. "Полювання за головами" (найвищий пріоритет)
            if (bountyEnabled && attackerRep >= Yggdrasil_ld.CONFIG.bounty_hunter_attacker_positive_rep_threshold && victimRep <= Yggdrasil_ld.CONFIG.bounty_hunter_victim_negative_rep_threshold) {
                double lossPercent = Yggdrasil_ld.CONFIG.bounty_hunter_victim_rep_loss_percentage / 100.0;
                int repLostByVictim = (int) Math.floor(Math.abs(victimRep * lossPercent));
                ReputationManager.addReputation(victim, repLostByVictim);

                int repGainedByAttacker = Yggdrasil_ld.CONFIG.reputation_pvp_kill_victim_bonus;
                int bonusSteps = repLostByVictim / Yggdrasil_ld.CONFIG.bounty_hunter_bonus_rep_step;
                repGainedByAttacker += bonusSteps * Yggdrasil_ld.CONFIG.bounty_hunter_bonus_rep_per_step;
                ReputationManager.addReputation(attacker, repGainedByAttacker);

                // 2. "Крадіжка репутації" (другий пріоритет)
            } else if (stealingEnabled && victimRep <= Yggdrasil_ld.CONFIG.reputation_stealing_threshold && attackerRep <= Yggdrasil_ld.CONFIG.reputation_stealing_threshold) {
                double transferPercent = Yggdrasil_ld.CONFIG.reputation_stealing_transfer_percentage / 100.0;
                double vanishPercent = Yggdrasil_ld.CONFIG.reputation_stealing_vanish_percentage / 100.0;
                int repToTransfer = (int) Math.floor(victimRep * transferPercent);
                int repToVanish = (int) Math.floor(victimRep * vanishPercent);
                ReputationManager.addReputation(attacker, repToTransfer);
                ReputationManager.removeReputation(victim, repToTransfer + repToVanish);

                // 3. НОВА "АНТИ-АБ'ЮЗ" ЛОГІКА (третій пріоритет)
            } else if (antiAbuseEnabled && attackerRep <= Yggdrasil_ld.CONFIG.anti_abuse_attacker_threshold && victimRep < 0) {
                double penaltyPercent = Yggdrasil_ld.CONFIG.anti_abuse_penalty_percentage / 100.0;
                int repChange = (int) Math.floor(victimRep * penaltyPercent); // Від'ємне число
                ReputationManager.addReputation(attacker, repChange); // Робимо репутацію вбивці ще гіршою

                // 4. Стандартна логіка (найнижчий пріоритет)
            } else {
                if (victimRep <= Yggdrasil_ld.CONFIG.reputation_pvp_kill_victim_bonus_threshold) {
                    ReputationManager.addReputation(attacker, Yggdrasil_ld.CONFIG.reputation_pvp_kill_victim_bonus);
                } else {
                    ReputationManager.removeReputation(attacker, Yggdrasil_ld.CONFIG.reputation_pvp_kill_penalty);
                }
            }
        }
    }
}

