package org.slavicmyths.textile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import org.slavicmyths.husbandry.Husbandry;

public final class Carcass extends Entity {
    public enum Kind {COW,PIG,SHEEP,RABBIT,GOOSE,DUCK,DOMESTIC_GOAT}
    private static final EntityDataAccessor<Integer> VARIANT=SynchedEntityData.defineId(Carcass.class,EntityDataSerializers.INT);
    private int life=6000;private boolean burning,processed;
    public Carcass(EntityType<? extends Carcass> type,Level l){super(type,l);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(VARIANT,0);}
    public Kind kind(){return Kind.values()[Math.max(0,Math.min(6,entityData.get(VARIANT)))];}
    public void setup(Kind k,boolean fire){entityData.set(VARIANT,k.ordinal());burning=fire;refreshDimensions();}
    public float visualScale(){return switch(kind()){case COW->1;case PIG->.9F;case SHEEP,DOMESTIC_GOAT->.8F;case GOOSE->.55F;case DUCK->.5F;case RABBIT->.35F;};}
    @Override public EntityDimensions getDimensions(Pose pose){return getType().getDimensions().scale(visualScale());}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key==VARIANT)refreshDimensions();}
    @Override public boolean isPickable(){return !isRemoved();}
    @Override public boolean isPushable(){return false;}
    @Override public void push(Entity e){}
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource d,float amount){return false;}
    @Override public void tick(){super.tick();if(!level().isClientSide){if(!onGround())move(MoverType.SELF,new net.minecraft.world.phys.Vec3(0,-.08,0));if(--life<=0)finish(false);}}
    @Override public InteractionResult interact(Player p,InteractionHand hand){
        ItemStack tool=p.getItemInHand(hand);
        if(p.isSpectator()||processed||!tool.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,Textiles.id("butchering_knives"))))return InteractionResult.PASS;
        if(!level().isClientSide){
            if(level() instanceof net.minecraft.server.level.ServerLevel server)server.sendParticles(new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM,new ItemStack(Textiles.item("animal_fat"))),getX(),getY()+.15,getZ(),3,.1,.05,.1,.01);
            finish(true);tool.hurtAndBreak(1,p,LivingEntity.getSlotForHand(hand));p.playSound(SoundEvents.SHEEP_SHEAR,.55F,.85F);}
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
    private void drop(Item item,int min,int max){int n=min+random.nextInt(max-min+1);if(n>0)spawnAtLocation(new ItemStack(item,n));}
    private void chance(Item item,float probability){if(random.nextFloat()<probability)drop(item,1,1);}
    private void meat(Item raw,Item cooked,int min,int max){drop(burning?cooked:raw,min,max);}
    private void finish(boolean enhanced){
        if(processed||isRemoved())return;processed=true;
        switch(kind()){
            case COW->{meat(Items.BEEF,Items.COOKED_BEEF,enhanced?4:2,enhanced?6:4);drop(Items.LEATHER,enhanced?1:0,enhanced?3:2);}
            case PIG->meat(Items.PORKCHOP,Items.COOKED_PORKCHOP,enhanced?4:1,enhanced?6:3);
            case SHEEP->meat(Items.MUTTON,Items.COOKED_MUTTON,enhanced?3:1,enhanced?5:2);
            case RABBIT->{meat(Items.RABBIT,Items.COOKED_RABBIT,1,enhanced?2:1);if(enhanced)drop(Items.RABBIT_HIDE,1,1);else chance(Items.RABBIT_HIDE,.5F);}
            case GOOSE->{meat(Husbandry.RAW_GOOSE.get(),Husbandry.COOKED_GOOSE.get(),enhanced?2:1,enhanced?4:2);drop(Items.FEATHER,enhanced?1:0,enhanced?3:2);}
            case DUCK->{meat(Husbandry.RAW_DUCK.get(),Husbandry.COOKED_DUCK.get(),enhanced?2:1,enhanced?4:2);drop(Items.FEATHER,enhanced?1:0,enhanced?3:2);}
            case DOMESTIC_GOAT->{meat(Husbandry.RAW_GOAT.get(),Husbandry.COOKED_GOAT.get(),enhanced?3:1,enhanced?5:3);if(enhanced)drop(Textiles.item("goat_hide"),1,1);}
        }
        if(enhanced){
            switch(kind()){
                case COW,SHEEP,DOMESTIC_GOAT->{drop(Textiles.item("animal_fat"),1,2);drop(Items.BONE,1,2);}
                case PIG->{drop(Textiles.item("animal_fat"),2,4);drop(Items.BONE,1,2);}
                case RABBIT->{chance(Textiles.item("animal_fat"),.25F);chance(Items.BONE,.5F);}
                case GOOSE->{drop(Textiles.item("animal_fat"),1,1);chance(Items.BONE,.5F);}
                case DUCK->{chance(Textiles.item("animal_fat"),.5F);chance(Items.BONE,.5F);}
            }
        }
        discard();
    }
    @Override protected void addAdditionalSaveData(CompoundTag n){n.putInt("Variant",kind().ordinal());n.putBoolean("WasBurning",burning);n.putInt("RemainingLife",life);}
    @Override protected void readAdditionalSaveData(CompoundTag n){entityData.set(VARIANT,Math.max(0,Math.min(6,n.getInt("Variant"))));burning=n.getBoolean("WasBurning");life=n.contains("RemainingLife")?Math.max(0,Math.min(6000,n.getInt("RemainingLife"))):6000;}
}
