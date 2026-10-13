package org.slavicmyths.water;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.progression.Knowledge;
public final class VodyanoyEntity extends WaterSpirit {
 private CompoundTag relations=new CompoundTag();
 public VodyanoyEntity(EntityType<? extends VodyanoyEntity> t,Level w){super(t,w);}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,44).add(Attributes.MOVEMENT_SPEED,.21).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.FOLLOW_RANGE,24);}
 public int reputation(Player p){return relations.getCompound(p.getStringUUID()).getInt("Rep");}
 public void pressure(Player p,int loss){if(p==null)return;String key=p.getStringUUID();if(!relations.contains(key)&&relations.getAllKeys().size()>=64)return;CompoundTag n=relations.getCompound(key);long now=level().getGameTime();if(now<n.getLong("PressureReady"))return;n.putLong("PressureReady",now+200);n.putInt("Rep",Math.max(-100,n.getInt("Rep")-loss));relations.put(key,n);if(n.getInt("Rep")<=-20)provoke(p);else voice("angry",.35F);}
 protected void think(){if(home==null)home=blockPosition();Player p=nearby(18);if(p!=null){Knowledge.award(p,"meet_vodyanoy");if(reputation(p)<=-20&&getTarget()==null)provoke(p);}if(getTarget()==null)wander();}
 protected InteractionResult mobInteract(Player p,InteractionHand h){if(level().isClientSide)return InteractionResult.SUCCESS;Knowledge.award(p,"meet_vodyanoy");ItemStack s=p.getItemInHand(h);Item i=s.getItem();if(i!=Items.BREAD&&i!=ModItems.KARAVAI.get()&&i!=ModItems.PEARL_FRAGMENT.get())return InteractionResult.PASS;String key=p.getStringUUID();if(!relations.contains(key)&&relations.getAllKeys().size()>=64)return InteractionResult.FAIL;CompoundTag n=relations.getCompound(key);long now=level().getGameTime();if(now<n.getLong("GiftReady"))return InteractionResult.FAIL;n.putInt("Rep",Math.min(100,n.getInt("Rep")+10));n.putLong("GiftReady",now+1200);relations.put(key,n);if(!p.isCreative())s.shrink(1);friend=p.getUUID();friendUntil=now+1200;if(getTarget()==p)setTarget(null);state(0);if(n.getInt("Rep")>=40&&!n.getBoolean("DepthClue")){n.putBoolean("DepthClue",true);relations.put(key,n);spawnAtLocation(ModItems.ANCIENT_WATER_SIGN.get());Knowledge.award(p,"depth_clue");}if(n.getInt("Rep")>=20){p.addEffect(new MobEffectInstance(MobEffects.LUCK,1800,0));if(random.nextInt(4)==0)spawnAtLocation(ModItems.PEARL_FRAGMENT.get());}voice("ambient",.5F);return InteractionResult.CONSUME;}
 public boolean hurt(DamageSource s,float amount){boolean hit=super.hurt(s,amount);if(hit&&!level().isClientSide&&s.getEntity() instanceof Player)pressure((Player)s.getEntity(),20);return hit;}
 public boolean doHurtTarget(Entity e){boolean hit=super.doHurtTarget(e);if(hit&&isInWater()&&level().getGameTime()>=nextPower){nextPower=level().getGameTime()+100;e.setDeltaMovement(e.getDeltaMovement().add(0,-.14,0));e.hurtMarked=true;}return hit;}
 public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.put("WaterRelations",relations);}
 public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);relations=n.getCompound("WaterRelations");}
}
