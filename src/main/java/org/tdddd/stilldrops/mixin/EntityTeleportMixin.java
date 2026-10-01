package org.tdddd.stilldrops.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tdddd.stilldrops.ConversionDrops;

/**
 * 钩子 A（主力）：EPCA 每完成一次转换，都会执行
 * entity.teleportTo(1000000, -4000, 1000000)。
 * 这个坐标在主模组里只出现在转换逻辑中，所以用它来判断转换。
 */
@Mixin(Entity.class)
public abstract class EntityTeleportMixin {

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"))
    private void stilldrops$onTeleport(double x, double y, double z, CallbackInfo ci) {
        ConversionDrops.onEntityTeleport((Entity) (Object) this, x, y, z);
    }
}
