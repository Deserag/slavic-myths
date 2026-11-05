package org.slavicmyths.sisters;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Weak Viy-only variants of the upyr and nav, plus root crawler. Never naturally generated. */
public final class ViyServant extends Monster {
    public enum Kind {
        UPYR("Могильный упырь",40,6,.27,1.20F,1.92F),
        NAV("Безликая навь",32,5,.32,.72F,2.12F),
        KORNELAP("Корнелап",24,4,.38,.92F,.72F);
        public final String label;
        public final double health,damage,speed;
        public final float width,height;
        Kind(String label,double health,double damage,double speed,float width,float height){
            this.label=label;this.health=health;this.damage=damage;this.speed=speed;this.width=width;this.height=height;
        }
    }
    private final Kind kind;
    private UUID owner;
    private int lifetime=2400;
    public ViyServant(EntityType<? extends ViyServant> type,Level world,Kind kind){
        super(type,world);this.kind=kind;setCustomName(Component.literal(kind.label));setPersistenceRequired();
    }
    public static AttributeSupplier.Builder attributes(Kind kind){
        return createMonsterAttributes().add(Attributes.MAX_HEALTH,kind.health)
            .add(Attributes.ATTACK_DAMAGE,kind.damage).add(Attributes.MOVEMENT_SPEED,kind.speed)
            .add(Attributes.FOLLOW_RANGE,28).add(Attributes.KNOCKBACK_RESISTANCE,.1);
    }
    public Kind kind(){return kind;}
    public UUID ownerId(){return owner;}
    public void link(ViyBoss viy,LivingEntity target){owner=viy.getUUID();setTarget(target instanceof Player player?player:null);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new MeleeAttackGoal(this,1.1,true));
        goalSelector.addGoal(5,new RandomStrollGoal(this,.7));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public void aiStep(){
        super.aiStep();
        if(!(level() instanceof ServerLevel world))return;
        if(owner==null||--lifetime<=0){discard();return;}
        if(tickCount%40==0){
            if(!(world.getEntity(owner) instanceof ViyBoss boss)||!boss.isAlive()){discard();return;}
            if(getTarget() instanceof Player p&&(p.isCreative()||p.isSpectator()))setTarget(null);
        }
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        if(owner!=null)tag.putUUID("ViyOwner",owner);
        tag.putInt("ViyServantLife",lifetime);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if(tag.hasUUID("ViyOwner"))owner=tag.getUUID("ViyOwner");
        if(tag.contains("ViyServantLife",3))lifetime=tag.getInt("ViyServantLife");
    }
}
