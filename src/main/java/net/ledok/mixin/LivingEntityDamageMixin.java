package net.ledok.mixin;

import net.ledok.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    public abstract World getWorld();

    @Inject(method = "damage", at = @At("HEAD"))
    private void yggdrasil_trackArenaDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity target = (LivingEntity) (Object) this;
        if (!getWorld().isClient() && source.getAttacker() != null) {
            // Find nearby mob arenas
            BlockPos.streamOutwards(target.getBlockPos(), 128, 128, 128).forEach(pos -> {
                if (getWorld().getBlockEntity(pos) instanceof MobSpawnerBlockEntity spawner) {
                    spawner.onMobDamaged(source, amount);
                }
            });
        }
    }
}

