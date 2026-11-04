package org.slavicmyths.sisters;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Spawn-egg-only Agneya preview. No worldgen or natural spawn is registered. */
public final class AgneyaBoss extends Monster {
    public AgneyaBoss(EntityType<? extends AgneyaBoss> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 300)
            .add(Attributes.MOVEMENT_SPEED, .27)
            .add(Attributes.ATTACK_DAMAGE, 14)
            .add(Attributes.ARMOR, 10)
            .add(Attributes.FOLLOW_RANGE, 32)
            .add(Attributes.KNOCKBACK_RESISTANCE, .65);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new RandomStrollGoal(this, .75));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
}