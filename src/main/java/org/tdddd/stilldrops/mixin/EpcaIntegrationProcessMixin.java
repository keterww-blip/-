package org.tdddd.stilldrops.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tdddd.stilldrops.ConversionDrops;

import java.util.List;

/**
 * 钩子 D：融合（多只生物合成新怪）。
 * 这些生物会被 discard() 掉，不走死亡流程，所以在这里补掉落。
 */
@Mixin(targets = "org.tdddd.epca.impl.overworld.data.EntityIntegrationManager$IntegrationProcess")
public abstract class EpcaIntegrationProcessMixin {

    @Shadow
    @Final
    private List<Mob> entities;

    @Inject(method = "completeIntegration", at = @At("HEAD"))
    private void stilldrops$completeIntegration(CallbackInfo ci) {
        ConversionDrops.onIntegration(this.entities);
    }
}
