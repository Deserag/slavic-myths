package org.slavicmyths.rpg;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.combat.MythDamageSources;

/** One non-AI, unsaved entity per active ability. Item snapshot and animation state are tracked normally. */
public final class RuneVisual extends Entity {
    public static final DeferredHolder<EntityType<?>,EntityType<RuneVisual>> TYPE=ModEntities.ENTITIES.register("rune_visual",()->EntityType.Builder.<RuneVisual>of(RuneVisual::new,MobCategory.MISC).sized(.4F,.4F).clientTrackingRange(12).updateInterval(1).noSave().build("slavicmyths:rune_visual"));
    private static final EntityDataAccessor<ItemStack> ITEM=SynchedEntityData.defineId(RuneVisual.class,EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> MODE=SynchedEntityData.defineId(RuneVisual.class,EntityDataSerializers.INT),OWNER=SynchedEntityData.defineId(RuneVisual.class,EntityDataSerializers.INT),COUNT=SynchedEntityData.defineId(RuneVisual.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(RuneVisual.class,EntityDataSerializers.INT);
    private UUID owner;private long expires;private int lifetime,attacks,dissolving;public int animationAge;private float damage;private LivingEntity target;private Vec3 start=Vec3.ZERO,aim=Vec3.ZERO;
    public RuneVisual(EntityType<? extends RuneVisual> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);}
    public static void init(){}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(ITEM,ItemStack.EMPTY);b.define(MODE,0);b.define(OWNER,0);b.define(COUNT,3);b.define(PHASE,0);}
    public static RuneVisual weapon(ServerPlayer p){var v=new RuneVisual(TYPE.get(),p.level());v.owner=p.getUUID();v.expires=RareRuneRuntime.now(p)+120;v.entityData.set(OWNER,p.getId());v.entityData.set(ITEM,p.getMainHandItem().copyWithCount(1));v.damage=(float)(p.getAttributeValue(Attributes.ATTACK_DAMAGE)*RareRuneRuntime.meleeMultiplier(p)*.70);v.setPos(p.position().add(shoulder(p)));return v;}
    public static RuneVisual shields(ServerPlayer p){var v=weapon(p);v.expires=RareRuneRuntime.now(p)+400;v.entityData.set(MODE,1);v.entityData.set(ITEM,ItemStack.EMPTY);return v;}
    public boolean ownedBy(ServerPlayer p){return p.getUUID().equals(owner);}
    public ItemStack item(){return entityData.get(ITEM);}public int mode(){return entityData.get(MODE);}public int count(){return entityData.get(COUNT);}public int phase(){return entityData.get(PHASE);}
    public LivingEntity ownerEntity(){return level().getEntity(entityData.get(OWNER)) instanceof LivingEntity l?l:null;}
    public void crack(int count,Vec3 incoming){entityData.set(COUNT,count);entityData.set(PHASE,10);if(incoming!=null)setYRot((float)(Math.atan2(incoming.z-getZ(),incoming.x-getX())*180/Math.PI));}
    public void dissolve(){if(dissolving==0){dissolving=8;entityData.set(PHASE,20);}}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key==PHASE)animationAge=0;}
    private static Vec3 shoulder(ServerPlayer p){double a=Math.toRadians(p.getYRot());return new Vec3(Math.cos(a)*.75,1.5,Math.sin(a)*.75);}
    private boolean valid(ServerPlayer p,LivingEntity t){return t!=null&&t.isAlive()&&p.distanceToSqr(t)<=144&&Abilities.canHit(p,t)&&p.hasLineOfSight(t);}
    private LivingEntity target(ServerPlayer p){var last=p.getLastHurtMob();if(valid(p,last))return last;return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(10),t->t instanceof Enemy&&valid(p,t)).stream().min(java.util.Comparator.comparingDouble(p::distanceToSqr)).orElse(null);}
    @Override public void tick(){super.tick();animationAge++;if(level().isClientSide)return;var p=owner==null?null:((ServerLevel)level()).getServer().getPlayerList().getPlayer(owner);
        if(p==null||!p.isAlive()||p.level()!=level()||!RareRunes.equipped(p,mode()==0?"floating_weapon":"rare_protection")){discard();return;}
        if(RareRuneRules.ready(RareRuneRuntime.now(p),expires))dissolve();
        if(dissolving>0){if(--dissolving==0){((ServerLevel)level()).sendParticles(ParticleTypes.ENCHANT,getX(),getY(),getZ(),5,.2,.2,.2,0);discard();}return;}
        lifetime++;
        if(mode()==1){setPos(p.position().add(0,1,0));if(lifetime>400||count()==0)dissolve();if(phase()==10&&lifetime%6==0)entityData.set(PHASE,0);return;}
        if(lifetime>=120||attacks>=3){dissolve();return;}
        int cycle=RareRuneRules.echoCycle(lifetime);if(lifetime<=12){setPos(p.position().add(shoulder(p)).add(0,Math.sin(lifetime*.3)*.06,0));entityData.set(PHASE,0);return;}
        if(cycle==0){target=target(p);start=p.position().add(shoulder(p));aim=valid(p,target)?target.position().add(0,target.getBbHeight()*.6,0):start;}
        if(cycle<6){entityData.set(PHASE,1);setPos(start);}
        else if(cycle<12){entityData.set(PHASE,2);setPos(start.lerp(aim,(cycle-5)/6.0));if(cycle==11){attacks++;if(valid(p,target)&&position().distanceToSqr(target.position().add(0,target.getBbHeight()*.6,0))<4){target.hurt(MythDamageSources.echo(this,p),damage);((ServerLevel)level()).sendParticles(ParticleTypes.ENCHANT,getX(),getY(),getZ(),3,.1,.1,.1,0);}}}
        else{entityData.set(PHASE,3);setPos(aim.lerp(p.position().add(shoulder(p)),Math.min(1,(cycle-11)/10.0)));}
        Vec3 direction=aim.subtract(start);setYRot((float)(Math.atan2(direction.z,direction.x)*180/Math.PI)-90);
    }
    @Override protected void readAdditionalSaveData(CompoundTag n){discard();}
    @Override protected void addAdditionalSaveData(CompoundTag n){}
    @Override public boolean isPickable(){return false;}
}
