package org.slavicmyths.husbandry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public final class YardAnimal extends Animal {
    private static final EntityDataAccessor<Integer> DEFENCE=SynchedEntityData.defineId(YardAnimal.class,EntityDataSerializers.INT);
    public final int kind;
    private int eggTimer;
    public int eatingTicks;
    private final GooseDefence defence;
    public YardAnimal(EntityType<? extends YardAnimal> type,Level level,int kind){
        super(type,level);this.kind=kind;eggTimer=newEggTimer();
        getNavigation().setCanFloat(true);
        goalSelector.addGoal(3,new TemptGoal(this,1.1,Ingredient.of(Husbandry.feed(new String[]{"goose","duck","domestic_goat"}[kind])),false));
        if(kind<2)goalSelector.addGoal(4,new NestGoal(this));
        if(kind==1)goalSelector.addGoal(6,new DuckWaterGoal(this));
        defence=kind==0?new GooseDefence(this):null;if(defence!=null)goalSelector.addGoal(1,defence);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(DEFENCE,0);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new PanicGoal(this,1.3));
        goalSelector.addGoal(3,new BreedGoal(this,1.0));goalSelector.addGoal(5,new YardParentGoal(this));
        goalSelector.addGoal(7,new WaterAvoidingRandomStrollGoal(this,1.0));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,6));goalSelector.addGoal(9,new RandomLookAroundGoal(this));
    }
    @Override public boolean isFood(ItemStack s){return s.is(Husbandry.feed(new String[]{"goose","duck","domestic_goat"}[kind]));}
    @Override public boolean canMate(Animal other){return other.getType()==getType()&&super.canMate(other);}
    @Override public AgeableMob getBreedOffspring(ServerLevel l,AgeableMob other){return other.getType()==getType()?(YardAnimal)getType().create(l):null;}
    @Override public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type){
        return kind<2&&type==net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value()?false:super.canDrownInFluidType(type);
    }
    @Override public InteractionResult mobInteract(Player p,InteractionHand hand){
        if(kind==2&&!isBaby()&&p.getItemInHand(hand).is(Items.BUCKET)){
            if(!level().isClientSide){p.playSound(SoundEvents.COW_MILK,1F,1.15F);p.setItemInHand(hand,ItemUtils.createFilledResult(p.getItemInHand(hand),p,new ItemStack(Husbandry.MILK.get())));}
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(p,hand);
    }
    @Override public void aiStep(){
        super.aiStep();if(eatingTicks>0)eatingTicks--;
        if(!level().isClientSide&&kind<2&&!isBaby()&&eggTimer>0)eggTimer--;
        if(!level().isClientSide&&kind==0&&!isBaby()&&tickCount%20==0&&defenceState()==0){
            for(YardAnimal baby:level().getEntitiesOfClass(YardAnimal.class,getBoundingBox().inflate(6),a->a.kind==0&&a.isBaby()&&distanceToSqr(a)<=36)){
                Player player=level().getNearestPlayer(baby.getX(),baby.getY(),baby.getZ(),2,true);
                if(player!=null){defence.warn(player,false);break;}
            }
        }
        if(kind<2&&isInWater()&&getDeltaMovement().y<0)setDeltaMovement(getDeltaMovement().multiply(1,.6,1));
    }
    public boolean eggReady(){return kind<2&&!isBaby()&&eggTimer<=0;}
    public ItemStack egg(){return new ItemStack(kind==0?Husbandry.GOOSE_EGG.get():Husbandry.DUCK_EGG.get());}
    private int newEggTimer(){return 9600+random.nextInt(8401);}
    public void resetEgg(){eggTimer=newEggTimer();}
    public void warn(Player p){if(defence!=null&&!isBaby())defence.warn(p,true);}
    public int defenceState(){return entityData.get(DEFENCE);}
    void setDefence(int state){entityData.set(DEFENCE,state);}
    public void eatAnimation(){level().broadcastEntityEvent(this,(byte)61);}
    @Override public void handleEntityEvent(byte event){if(event==61)eatingTicks=20;else super.handleEntityEvent(event);}
    @Override public boolean hurt(DamageSource source,float damage){
        boolean result=super.hurt(source,damage);
        if(result&&!level().isClientSide&&kind==0&&source.getEntity() instanceof Player p){
            if(isBaby()){
                for(YardAnimal adult:level().getEntitiesOfClass(YardAnimal.class,getBoundingBox().inflate(6),a->a.kind==0&&!a.isBaby()&&distanceToSqr(a)<=36))adult.warn(p);
            }else warn(p);
        }
        return result;
    }
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(kind<2)n.putInt("EggTimer",eggTimer);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);if(kind<2&&n.contains("EggTimer"))eggTimer=Math.max(0,Math.min(18000,n.getInt("EggTimer")));setDefence(0);}
    @Override protected SoundEvent getAmbientSound(){return (kind==0?Husbandry.GOOSE_AMBIENT:kind==1?Husbandry.DUCK_AMBIENT:Husbandry.GOAT_AMBIENT).get();}
    @Override protected SoundEvent getHurtSound(DamageSource d){return (kind==0?Husbandry.GOOSE_HURT:kind==1?Husbandry.DUCK_HURT:Husbandry.GOAT_HURT).get();}
    @Override protected SoundEvent getDeathSound(){return (kind==0?Husbandry.GOOSE_DEATH:kind==1?Husbandry.DUCK_DEATH:Husbandry.GOAT_DEATH).get();}
}
