package org.tdddd.stilldrops;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 掉落逻辑 + 各个钩子的入口。
 * 所有钩子最终都走 {@link #dropLoot}，并且做了「同一只生物只掉一次」的去重。
 */
public final class ConversionDrops {

    /** EPCA 转换生物时，会把原生物“流放”到这个坐标。 */
    private static final double EXILE_X = 1000000.0D;
    private static final double EXILE_Y = -4000.0D;
    private static final double EXILE_Z = 1000000.0D;

    /** 已经掉过的生物 id，避免同一次转换被多个钩子重复处理。 */
    private static final Set<Integer> HANDLED = new HashSet<>();
    private static final int HANDLED_LIMIT = 65536;

    private ConversionDrops() {
    }

    // ------------------------------------------------------------------
    // 钩子入口
    // ------------------------------------------------------------------

    /** 钩子 B / C / D：EPCA 的转换方法刚被调用，生物还没被移除。 */
    public static void onPreConversion(LivingEntity entity) {
        // 一次转换里生物可能先被移除、又被另一条路径处理一遍，这时跳过
        if (entity.isRemoved()) {
            return;
        }
        dropLoot(entity);
    }

    /** 钩子 C 之子项：数据包规则转换。目标不存在时不会真的转换，所以先校验一次。 */
    public static void onDataPackConversion(LivingEntity entity, String targetEntityId) {
        if (entity.isRemoved() || targetEntityId == null || targetEntityId.isEmpty()) {
            return;
        }
        ResourceLocation target = ResourceLocation.tryParse(targetEntityId);
        if (target == null || ForgeRegistries.ENTITY_TYPES.getValue(target) == null) {
            return;
        }
        dropLoot(entity);
    }

    /** 钩子 A：生物被传送到“流放坐标” = EPCA 刚刚完成一次转换。 */
    public static void onEntityTeleport(Entity entity, double x, double y, double z) {
        if (x != EXILE_X || y != EXILE_Y || z != EXILE_Z) {
            return;
        }
        // 已经在流放坐标上 = 同一只生物被转换了第二次，跳过
        if (Math.abs(entity.getX() - EXILE_X) < 1.0D
                && Math.abs(entity.getY() - EXILE_Y) < 1.0D
                && Math.abs(entity.getZ() - EXILE_Z) < 1.0D) {
            return;
        }
        if (entity instanceof LivingEntity living) {
            dropLoot(living);
        }
    }

    /** 钩子 D：融合（多只生物合成新怪）完成之前，逐个处理。 */
    public static void onIntegration(List<Mob> entities) {
        if (entities == null) {
            return;
        }
        for (Mob mob : entities) {
            if (!mob.isRemoved()) {
                dropLoot(mob);
            }
        }
    }

    // ------------------------------------------------------------------
    // 掉落
    // ------------------------------------------------------------------

    private static void dropLoot(LivingEntity original) {
        if (!(original.level() instanceof ServerLevel level)) {
            return;
        }
        // 玩家没有掉落表
        if (original instanceof Player) {
            return;
        }
        // 尊重原版规则：关掉生物掉落时不掉
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
                || !level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        // 同一只生物只掉一次
        if (!markHandled(original.getId())) {
            return;
        }

        ResourceLocation lootTableId = original.getLootTable();
        if (lootTableId == null) {
            return;
        }

        // 最后打它的是玩家 -> 按“玩家击杀”算，掠夺附魔 / 仅玩家掉落 / 幸运 都正常生效
        Player killer = original.getLastHurtByMob() instanceof Player player ? player : null;

        DamageSource damageSource = killer != null
                ? level.damageSources().playerAttack(killer)
                : level.damageSources().generic();

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, original)
                .withParameter(LootContextParams.ORIGIN, original.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, killer)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, killer)
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer)
                .withLuck(killer != null ? killer.getLuck() : 0.0F)
                .create(LootContextParamSets.ENTITY);

        LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);
        List<ItemStack> drops = lootTable.getRandomItems(params);

        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                original.spawnAtLocation(stack);
            }
        }
    }

    private static boolean markHandled(int entityId) {
        if (HANDLED.size() > HANDLED_LIMIT) {
            HANDLED.clear();
        }
        return HANDLED.add(entityId);
    }
}
