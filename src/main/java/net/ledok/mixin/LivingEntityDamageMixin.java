package net.ledok.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityDamageMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float modifyDamage(float amount, DamageSource source) {
        // All boss level and damage scaling logic has been removed for now.
        return amount;
    }
}

