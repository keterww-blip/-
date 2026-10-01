package org.tdddd.stilldrops.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tdddd.stilldrops.ConversionDrops;

/**
 * 钩子 B：EPCA 的通用转换
 * （小型 / 中型 / 大型未完成体、污染水）。
 */
@Mixin(targets = "org.tdddd.epca.impl.utils.EntityConversionUtil")
public abstract class EpcaConversionUtilMixin {

    @Inject(method = "convertTo", at = @At("HEAD"))
    private static void stilldrops$convertTo(LivingEntity entity, EntityType<?> targetType, CallbackInfo ci) {
        ConversionDrops.onPreConversion(entity);
    }
}
