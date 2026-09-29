package org.slavicmyths.entity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.slavicmyths.registry.ModItems;
public final class BannikEntity extends LandSpiritEntity {
    private int warnings;private long nextStone;
    public BannikEntity(EntityType<? extends BannikEntity> t,World w){super(t,w);}
    public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,42).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.KNOCKBACK_RESISTANCE,.7).add(Attributes.FOLLOW_RANGE,16);}
    public boolean offer(PlayerEntity p,Hand hand){
        ItemStack s=p.getItemInHand(hand);boolean water=s.getItem()==Items.POTION && PotionUtils.getPotion(s)==Potions.WATER;
        if(s.getItem()!=ModItems.BATH_BROOM.get() && s.getItem()!=Items.BREAD && !water)return false;
        if(!level.isClientSide){if(!p.abilities.instabuild){s.shrink(1);if(water){ItemStack bottle=new ItemStack(Items.GLASS_BOTTLE);if(!p.inventory.add(bottle))p.drop(bottle,false);}}friend=p.getUUID();friendUntil=level.getGameTime()+12000;setTarget(null);state(0);warnings=0;windup=0;particles(ParticleTypes.HAPPY_VILLAGER,6);p.displayClientMessage(new net.minecraft.util.text.TranslationTextComponent("spirit.slavicmyths.bannik_peace"),true);}
        return true;
    }
    @Override protected ActionResultType mobInteract(PlayerEntity p,Hand h){return offer(p,h)?ActionResultType.sidedSuccess(level.isClientSide):super.mobInteract(p,h);}
    @Override protected void think(){
        if(home==null){home=blockPosition();setPersistenceRequired();}
        if(getTarget()!=null){
            if(windup==0 && level.getGameTime()>nextPower && distanceToSqr(getTarget())<49){windup=15;state(4);getNavigation().stop();voice("power",.7F);nextPower=level.getGameTime()+360;}
            if(windup==0 && level.getGameTime()>nextStone && distanceToSqr(getTarget())>16 && canSee(getTarget())){HotStoneEntity stone=new HotStoneEntity(level,this);double dx=getTarget().getX()-getX(),dz=getTarget().getZ()-getZ();stone.shoot(dx,getTarget().getY()+1-stone.getY()+Math.sqrt(dx*dx+dz*dz)*.15,dz,1.1F,3);level.addFreshEntity(stone);nextStone=level.getGameTime()+160;}
            return;
        }
        PlayerEntity p=nearby(8);if(p==null || friendly(p)){warnings=0;return;}
        getLookControl().setLookAt(p,30,30);warnings+=20;if(warnings%100==0){voice("angry",.45F);p.displayClientMessage(new net.minecraft.util.text.TranslationTextComponent("spirit.slavicmyths.bannik_warning"),true);}
        if(warnings>=300)provoke(p);
    }
    @Override protected void abilityTick(){if(windup>0){getNavigation().stop();if(--windup==0){particles(ParticleTypes.CLOUD,45);for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(5),p->!p.isCreative() && !p.isSpectator() && canSee(p) && p.position().subtract(position()).normalize().dot(getLookAngle())>.35)){p.hurt(DamageSource.mobAttack(this),3);p.addEffect(new EffectInstance(Effects.BLINDNESS,40));p.knockback(.5F,getX()-p.getX(),getZ()-p.getZ());}state(3);}}}
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundNBT n){super.addAdditionalSaveData(n);n.putLong("NextStone",nextStone);}
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundNBT n){super.readAdditionalSaveData(n);nextStone=n.getLong("NextStone");}
}
