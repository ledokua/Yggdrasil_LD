package net.ledok.mixin;

import net.ledok.block.entity.MobSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    // Method to get the level, which will be implemented by the parent class
    public abstract Level level();

    @Inject(method = "hurt", at = @At("HEAD"))
    private void yggdrasil_trackArenaDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity target = (LivingEntity) (Object) this;
        // In Mojang mappings, isClient() is now isClientSide()
        if (!level().isClientSide() && source.getEntity() != null) {
            // Find nearby mob arenas using a modern streaming method
            BlockPos.withinManhattanStream(target.blockPosition(), 128, 128, 128).forEach(pos -> {
                if (level().getBlockEntity(pos) instanceof MobSpawnerBlockEntity spawner) {
                    spawner.onMobDamaged(source, amount);
                }
            });
        }
    }
}
