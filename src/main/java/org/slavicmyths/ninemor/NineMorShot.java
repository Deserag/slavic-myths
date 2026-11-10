package org.slavicmyths.ninemor;

import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Boss projectiles stay unchanged; the player-summoned variant visibly forms and launches in sequence. */
public final class NineMorShot extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> FORMATION=SynchedEntityData.defineId(NineMorShot.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> LAUNCHED=SynchedEntityData.defineId(NineMorShot.class,EntityDataSerializers.BOOLEAN);
    private UUID markedTarget;
    private int hoveringTicks;

    public NineMorShot(EntityType<? extends NineMorShot> type, Level level) { super(type,level); }
    public NineMorShot(Level level,LivingEntity owner,int element) {
        super(NineMorContent.SHOT.get(),owner,level);
        setItem(new ItemStack(visualItem(element)));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FORMATION,0);
        builder.define(LAUNCHED,false);
    }
    public boolean isSummonedSpear() {return entityData.get(FORMATION)>0;}
    public boolean spearLaunched() {return entityData.get(LAUNCHED);}
    public void summonBehind(LivingEntity target,int index) {
        if(level().isClientSide||index<0||index>=4)return;
        markedTarget=target.getUUID();
        hoveringTicks=16+index*7;
        entityData.set(FORMATION,index+1);
        entityData.set(LAUNCHED,false);
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
    }
    private static Item visualItem(int element) {
        return switch(element) {
            case 1 -> Items.LIME_DYE;
            case 2 -> Items.PURPLE_DYE;
            case 3 -> Items.CYAN_DYE;
            case 4 -> Items.ORANGE_DYE;
            case 5 -> Items.YELLOW_DYE;
            case 6 -> Items.GREEN_DYE;
            case 7 -> Items.BROWN_DYE;
            case 8 -> Items.BLUE_DYE;
            case 9 -> NineMorContent.SPEAR.get();
            default -> Items.RED_DYE;
        };
    }
    private int type() {
        Item stackItem=getItem().getItem();
        if(stackItem==NineMorContent.SPEAR.get())return 2;
        for(int i=1;i<=8;i++)if(stackItem==visualItem(i))return i;
        return 0;
    }
    @Override protected Item getDefaultItem() {return Items.RED_DYE;}
    @Override protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if(!level().isClientSide&&hit.getEntity() instanceof LivingEntity living&&hit.getEntity()!=getOwner()
            &&!(living instanceof NineMorSerpent)
            &&!(getOwner() instanceof Player p&&(p.isAlliedTo(living)||living.isAlliedTo(p)
                ||living instanceof Player other&&(other.isCreative()||other.isSpectator()||!p.canHarmPlayer(other))))) {
            LivingEntity attacker=getOwner() instanceof LivingEntity other?other:null;
            boolean landed=living.hurt(attacker instanceof Player player?damageSources().playerAttack(player)
                :attacker!=null?damageSources().mobAttack(attacker):damageSources().magic(),isSummonedSpear()?14F:8F);
            if(landed){
                NineMorSerpent.applyElement(living,type());
                if(isSummonedSpear()){
                    living.addEffect(new MobEffectInstance(MobEffects.WITHER,100,1));
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,65,1));
                }
            }
        }
        impact();
    }
    @Override protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        impact();
    }
    private void impact() {
        if(level() instanceof ServerLevel server) {
            int color=NineMorSerpent.COLORS[type()];
            server.sendParticles(new DustParticleOptions(new Vector3f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F),1.05F),
                getX(),getY(),getZ(),isSummonedSpear()?8:23,.22D,.22D,.22D,.025D);
            discard();
        }
    }
    @Override public void tick() {
        if(level() instanceof ServerLevel server&&isSummonedSpear()) {
            if(!(getOwner() instanceof ServerPlayer owner)||!owner.isAlive()||owner.level()!=level()){discard();return;}
            LivingEntity target=markedTarget==null?null:server.getEntity(markedTarget) instanceof LivingEntity living?living:null;
            if(target==null||!NineMorRewardAbilities.eligible(owner,target)||target.distanceToSqr(owner)>40D*40D){discard();return;}
            if(!spearLaunched()) {
                Vec3 forward=owner.getLookAngle().multiply(1D,0D,1D).normalize();
                if(forward.lengthSqr()<.01D)forward=new Vec3(0D,0D,1D);
                Vec3 right=new Vec3(-forward.z,0D,forward.x);
                int index=entityData.get(FORMATION)-1;
                Vec3 anchor=owner.position().add(0D,2.65D+(index%2)*.33D+Math.sin(tickCount*.18D+index)*.08D,0D)
                    .subtract(forward.scale(2.3D)).add(right.scale((index-1.5D)*1.05D));
                setPos(anchor.x,anchor.y,anchor.z);
                setDeltaMovement(Vec3.ZERO);
                if(--hoveringTicks<=0) {
                    Vec3 aim=target.getBoundingBox().getCenter().subtract(position());
                    if(aim.lengthSqr()>.01D){
                        entityData.set(LAUNCHED,true);
                        shoot(aim.x,aim.y,aim.z,2.6F,0F);
                    }else{discard();return;}
                }
            }else{
                Vec3 desired=target.getBoundingBox().getCenter().subtract(position());
                if(desired.lengthSqr()>.01D)setDeltaMovement(getDeltaMovement().lerp(desired.normalize().scale(2.6D),.18D));
            }
        }
        super.tick();
        if(!level().isClientSide&&tickCount>(isSummonedSpear()?78:100))discard();
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("NineMorFormation",entityData.get(FORMATION));
        tag.putBoolean("NineMorLaunched",spearLaunched());
        tag.putInt("NineMorHoveringTicks",hoveringTicks);
        if(markedTarget!=null)tag.putUUID("NineMorMarkedTarget",markedTarget);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(FORMATION,tag.getInt("NineMorFormation"));
        entityData.set(LAUNCHED,tag.getBoolean("NineMorLaunched"));
        hoveringTicks=Math.max(0,Math.min(48,tag.getInt("NineMorHoveringTicks")));
        markedTarget=tag.hasUUID("NineMorMarkedTarget")?tag.getUUID("NineMorMarkedTarget"):null;
    }
}
