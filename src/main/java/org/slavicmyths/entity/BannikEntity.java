package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.ModItems;
public final class BannikEntity extends LandSpiritEntity {
    private int warnings;private long nextStone;
    public BannikEntity(EntityType<? extends BannikEntity> t,Level w){super(t,w);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,42).add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.KNOCKBACK_RESISTANCE,.7).add(Attributes.FOLLOW_RANGE,16);}
    public boolean offer(Player p,InteractionHand hand){
        ItemStack s=p.getItemInHand(hand);boolean water=s.getItem()==Items.POTION && s.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS,net.minecraft.world.item.alchemy.PotionContents.EMPTY).is(net.minecraft.world.item.alchemy.Potions.WATER);
        if(s.getItem()!=ModItems.BATH_BROOM.get() && s.getItem()!=Items.BREAD && !water)return false;
        if(!level().isClientSide){if(!p.getAbilities().instabuild){s.shrink(1);if(water){ItemStack bottle=new ItemStack(Items.GLASS_BOTTLE);if(!p.getInventory().add(bottle))p.drop(bottle,false);}}friend=p.getUUID();friendUntil=level().getGameTime()+(org.slavicmyths.rpg.PathData.has(p,"offering")?13800:12000);setTarget(null);state(0);warnings=0;windup=0;particles(ParticleTypes.HAPPY_VILLAGER,6);p.displayClientMessage(Component.translatable("spirit.slavicmyths.bannik_peace"),true);}
        return true;
    }
    @Override protected InteractionResult mobInteract(Player p,InteractionHand h){return offer(p,h)?InteractionResult.sidedSuccess(level().isClientSide):super.mobInteract(p,h);}
    @Override protected void think(){
        if(home==null){home=blockPosition();setPersistenceRequired();}
        if(getTarget()!=null){
            if(windup==0 && level().getGameTime()>nextPower && distanceToSqr(getTarget())<49){windup=15;state(4);getNavigation().stop();voice("steam",.6F);nextPower=level().getGameTime()+360;}
            if(windup==0 && level().getGameTime()>nextStone && distanceToSqr(getTarget())>16 && hasLineOfSight(getTarget())){HotStoneEntity stone=new HotStoneEntity(level(),this);double dx=getTarget().getX()-getX(),dz=getTarget().getZ()-getZ();stone.shoot(dx,getTarget().getY()+1-stone.getY()+Math.sqrt(dx*dx+dz*dz)*.15,dz,1.1F,3);level().addFreshEntity(stone);nextStone=level().getGameTime()+160;}
            return;
        }
        Player p=nearby(8);if(p==null || friendly(p)){warnings=0;return;}
        getLookControl().setLookAt(p,30,30);int oldWarnings=warnings;warnings+=org.slavicmyths.rpg.PathData.has(p,"quiet_step")?15:20;if(warnings/100>oldWarnings/100){voice("angry",.45F);p.displayClientMessage(Component.translatable("spirit.slavicmyths.bannik_warning"),true);}
        if(warnings>=300)provoke(p);
    }
    @Override protected void abilityTick(){if(windup>0){getNavigation().stop();if(--windup==0){particles(ParticleTypes.CLOUD,45);for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(5),p->!p.isCreative() && !p.isSpectator() && hasLineOfSight(p) && p.position().subtract(position()).normalize().dot(getLookAngle())>.35)){p.hurt(damageSources().mobAttack(this),3);p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,40));p.knockback(.5F,getX()-p.getX(),getZ()-p.getZ());}state(3);}}}
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag n){super.addAdditionalSaveData(n);n.putLong("NextStone",nextStone);}
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag n){super.readAdditionalSaveData(n);nextStone=n.getLong("NextStone");}
    @Override public int getAmbientSoundInterval(){return 560;}
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState block){if(!level().isClientSide)voice("step",.2F);}
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit && !level().isClientSide)voice("attack",.45F);return hit;}
}
