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
    public enum Behavior { DORMANT, WARNING, HOSTILE, STEAM_PREP, STEAM_BURST, SLAP_COMBO, HEAT_ENRAGE, RECOVERY }
    private final AttackTimeline attack=new AttackTimeline();private Behavior behavior=Behavior.DORMANT;private boolean steam;
    public Behavior behavior(){return behavior;}
    public BannikEntity(EntityType<? extends BannikEntity> t,Level w){super(t,w);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,80).add(Attributes.MOVEMENT_SPEED,.26).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.ARMOR,5).add(Attributes.KNOCKBACK_RESISTANCE,.7).add(Attributes.FOLLOW_RANGE,20);}
    @Override protected boolean meleeReady(){return false;}
    @Override protected AttackTimeline attackTimeline(){return attack;}
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
            behavior=getHealth()<getMaxHealth()*.35F?Behavior.HEAT_ENRAGE:Behavior.HOSTILE;
            return;
        }
        Player p=nearby(8);if(p==null || friendly(p)){warnings=0;behavior=Behavior.DORMANT;return;}
        behavior=Behavior.WARNING;
        getLookControl().setLookAt(p,30,30);int oldWarnings=warnings;warnings+=org.slavicmyths.rpg.PathData.has(p,"quiet_step")?15:20;if(warnings/100>oldWarnings/100){voice("angry",.45F);p.displayClientMessage(Component.translatable("spirit.slavicmyths.bannik_warning"),true);}
        if(warnings>=300)provoke(p);
    }
    @Override protected void abilityTick(){
        if(getTarget()==null){attack.cancel();return;}
        boolean wet=isInWaterOrBubble()||level().isRainingAt(blockPosition());
        if(steam&&wet&&attack.phase()==AttackTimeline.Phase.TELEGRAPH){attack.cancel();nextPower=level().getGameTime()+240;particles(ParticleTypes.CLOUD,8);return;}
        if(attack.ready()){
            if(!wet&&level().getGameTime()>=nextPower&&attackable(getTarget(),5)){steam=true;attack.start(24,1,20,40);behavior=Behavior.STEAM_PREP;state(4);nextPower=level().getGameTime()+240;voice("steam",.6F);}
            else if(attackable(getTarget(),2.6)){steam=false;attack.start(14,1,18,getHealth()<getMaxHealth()*.35F?34:40);behavior=Behavior.SLAP_COMBO;voice("attack",.5F);}
            else{chase(1);return;}
        }
        if(attack.phase()==AttackTimeline.Phase.TELEGRAPH){faceTarget();getNavigation().stop();if(steam&&tickCount%4==0)particles(ParticleTypes.CLOUD,2);}
        if(attack.tick()){
            if(steam){behavior=Behavior.STEAM_BURST;particles(ParticleTypes.CLOUD,45);for(Player player:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(5),p->attackable(p,5)&&p.position().subtract(position()).multiply(1,0,1).normalize().dot(getLookAngle().multiply(1,0,1).normalize())>.35)){player.hurt(damageSources().mobAttack(this),4);player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,40));}}
            else strikeTarget(8,2.6,.6F);
        }
        if(attack.phase()==AttackTimeline.Phase.RECOVERY){behavior=Behavior.RECOVERY;state(3);getNavigation().stop();}
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag n){super.addAdditionalSaveData(n);n.putLong("NextStone",nextStone);}
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag n){super.readAdditionalSaveData(n);nextStone=n.getLong("NextStone");}
    @Override public int getAmbientSoundInterval(){return 560;}
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState block){if(!level().isClientSide)voice("step",.2F);}
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity target){boolean hit=super.doHurtTarget(target);if(hit && !level().isClientSide)voice("attack",.45F);return hit;}
}
