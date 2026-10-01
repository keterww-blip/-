package org.tdddd.stilldrops.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tdddd.stilldrops.ConversionDrops;

/**
 * 钩子 C：EPCA 的 CothEffect 里的两条转换路径
 * （数据包规则转换、转成非生物）。
 */
@Mixin(targets = "org.tdddd.epca.impl.overworld.registry.effects.debuff.CothEffect")
public abstract class EpcaCothEffectMixin {

    @Inject(method = "convertUsingDataPackRule", at = @At("HEAD"))
    private static void stilldrops$dataPackRule(LivingEntity entity, String targetEntity, CallbackInfo ci) {
        ConversionDrops.onDataPackConversion(entity, targetEntity);
    }

    @Inject(method = "convertToNonLivingEntity", at = @At("HEAD"))
    private static void stilldrops$nonLiving(LivingEntity originalEntity, EntityType<?> targetType, CallbackInfo ci) {
        ConversionDrops.onPreConversion(originalEntity);
    }
}
